import os
import pandas as pd
import yaml
import numpy as np
import shutil


dataset_root = "/path/to/your/dataset"
image_root = os.path.join(dataset_root, "train")
export_root = os.path.join(dataset_root, "export")
num_clients = 4
iid = False
alpha = 0.5
seed = 0
n_classes = 4
class_names = [
    "CNV",
    "DME",
    "DRUSEN",
    "NORMAL",
]


def set_seed(seed):
    np.random.seed(seed)


def partation_data(data, num_clients, iid, alpha):
    num_samples = len(data)

    if iid:
        num_samples_per_client = int(num_samples / num_clients)
        indices = np.arange(num_samples)
        np.random.shuffle(indices)
        client_data = {
            i: data.iloc[
                indices[i * num_samples_per_client : (i + 1) * num_samples_per_client]
            ]
            for i in range(num_clients)
        }

    else:

        label_distribution = np.random.dirichlet([alpha] * num_clients, n_classes)

        class_idcs = [
            np.argwhere(np.array(data.category) == i).flatten()
            for i in range(n_classes)
        ]

        client_idcs = [[] for _ in range(num_clients)]

        for c, fracs in zip(class_idcs, label_distribution):
            for i, idcs in enumerate(
                np.split(c, (np.cumsum(fracs)[:-1] * len(c)).astype(int))
            ):
                client_idcs[i] += [idcs]

        client_idcs = [np.concatenate(idcs) for idcs in client_idcs]

        client_data = {i: data.iloc[client_idcs[i]] for i in range(num_clients)}

    return client_data


def main():
    set_seed(seed)
    data = pd.DataFrame(
        [
            (os.path.join(image_root, "CNV", f), "CNV")
            for f in os.listdir(os.path.join(image_root, "CNV"))
        ]
        + [
            (os.path.join(image_root, "DME", f), "DME")
            for f in os.listdir(os.path.join(image_root, "DME"))
        ]
        + [
            (os.path.join(image_root, "DRUSEN", f), "DRUSEN")
            for f in os.listdir(os.path.join(image_root, "DRUSEN"))
        ]
        + [
            (os.path.join(image_root, "NORMAL", f), "NORMAL")
            for f in os.listdir(os.path.join(image_root, "NORMAL"))
        ],
        columns=["name", "category"],
    )

    data.category = data.category.map({"CNV": 0, "DME": 1, "DRUSEN": 2, "NORMAL": 3})

    client_data = partation_data(data, num_clients, iid, alpha)

    export_root = os.path.join(
        dataset_root, "export", "iid" if iid else f"non-iid_{alpha}"
    )
    os.makedirs(export_root, exist_ok=True)

    for i in range(num_clients):

        client_export_root = os.path.join(export_root, f"client_{i}")
        os.makedirs(client_export_root, exist_ok=True)

        print(f"image root: {image_root}")
        print(f"client export root: {client_export_root}")

        # os.makedirs(os.path.join(client_export_root, "images"), exist_ok=True)

        # images, meta.yaml, train.csv

        client_data[i].rename(columns={"category": "label"}, inplace=True)
        client_data[i].label = client_data[i].label.map(
            {0: "CNV", 1: "DME", 2: "DRUSEN", 3: "NORMAL"}
        )

        # client_data[i].label = client_data[i].label.astype(int)
        # only list the file name
        # client_data[i].name = (
        #     client_data[i]
        #     .name.astype(str)
        #     .str.split("\\")
        #     .str[-1]
        #     .str.split(".")
        #     .str[0]
        # )

        # client_data[i].to_csv(
        #     os.path.join(client_export_root, "train.csv"), index=False
        # )

        for cls_name in class_names:
            os.makedirs(os.path.join(client_export_root, cls_name), exist_ok=True)

        for image_nm, image_cat in zip(client_data[i].name, client_data[i].label):
            # image_nm = image_nm + ".jpg"
            # image_cat = ["CNV", "DME", "DRUSEN", "NORMAL"][image_cat]

            # print(
            #     f"copying {os.path.join(image_root, image_cat, image_nm)} to {os.path.join(client_export_root, image_cat, os.path.basename(image_nm))}"
            # )
            shutil.copy(
                os.path.join(image_root, image_cat, image_nm),
                os.path.join(client_export_root, image_cat, os.path.basename(image_nm)),
            )

        # zip file
        shutil.copy("convert.py", os.path.join(client_export_root, "convert.py"))
        os.system(f"cd {client_export_root} && python convert.py")

        print(f"Client {i} has {len(client_data[i])} samples exported.")


def preprocess():
    # preprocess: change the image extension to jpg
    for category in ["CNV", "DME", "DRUSEN", "NORMAL"]:
        for f in os.listdir(os.path.join(image_root, category)):
            os.rename(
                os.path.join(image_root, category, f),
                os.path.join(image_root, category, f.split(".")[0] + ".jpg"),
            )


if __name__ == "__main__":
    # preprocess()
    main()
