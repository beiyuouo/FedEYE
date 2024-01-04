import os
import pandas as pd
import yaml
import numpy as np
import shutil


dataset_root = "/path/to/your/dataset"
image_root = os.path.join(dataset_root, "images")
export_root = os.path.join(dataset_root, "export")
num_clients = 4
iid = True
alpha = 0.5
seed = 0
n_classes = 4
class_names = [
    "point_like_corneal_ulcers",
    "point_flaky_mixed_corneal_ulcers",
    "flaky_corneal_ulcers",
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
    data = pd.read_csv(dataset_root + "/labels.csv")
    client_data = partation_data(data, num_clients, iid, alpha)

    export_root = os.path.join(
        dataset_root, "export", "iid" if iid else f"non-iid_{alpha}"
    )
    if not os.path.exists(export_root):
        os.makedirs(export_root)
    else:
        print(f"Export root {export_root} already exists. Overwriting...")
        shutil.rmtree(export_root)
        os.makedirs(export_root)

    for i in range(num_clients):
        client_export_root = os.path.join(export_root, f"client_{i}")
        os.makedirs(client_export_root, exist_ok=True)

        for cls_name in class_names:
            os.makedirs(os.path.join(client_export_root, cls_name), exist_ok=True)

        # os.makedirs(os.path.join(client_export_root, "images"), exist_ok=True)

        # images, meta.yaml, train.csv

        # client_data columns: name, category, type, grade -> name, label
        client_data[i] = client_data[i].rename(columns={"category": "label"})

        client_data[i].drop(columns=["type", "grade"], inplace=True)
        client_data[i].label = client_data[i].label.astype(int)
        client_data[i].name = client_data[i].name.astype(str).str.replace(".jpg", "")

        for row in client_data[i].itertuples():
            shutil.copy(
                os.path.join(image_root, row.name + ".jpg"),
                os.path.join(
                    client_export_root, class_names[row.label], row.name + ".jpg"
                ),
            )

        # zip file
        shutil.copy("convert.py", os.path.join(client_export_root, "convert.py"))
        os.system(f"cd {client_export_root} && python convert.py")

        print(f"Client {i} has {len(client_data[i])} samples exported.")


if __name__ == "__main__":
    main()
