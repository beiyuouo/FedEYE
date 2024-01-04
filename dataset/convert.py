# -*- coding: UTF-8 -*-
r"""
@Author      : wlhuang
@Date        : 2023/5/9 14:10
@Institution : CUbiCS, ICT, CAS
@Contact     : wuliang.huang@outlook.com
@Description : Convert participant dataset to Aier platform compatible format.
"""
import re
import shutil
from pathlib import Path

yaml_content = r"""
attributes:
  evalType: multi
  name: 独立科研_用户数据
  note: 独立科研_用户数据
inputs:
  ext: jpg
  pilMode: RGB
  type: images
labelMapping:
%mapping%
targets:
- name: label
  type: integer
type: vision
"""


def move_images():
    root = Path(__file__).parent
    task_name = root.stem
    classes = [name for name in root.iterdir() if name.is_dir()]
    name_to_id = {name: index for index, name in enumerate(classes)}
    mappings = "\n".join(
        [
            f"- label: '{index}'\n  mapped: {name.stem}"
            for name, index in name_to_id.items()
        ]
    )
    temp_folder = root / f"{task_name}_用户数据"
    temp_folder.mkdir(exist_ok=True)
    image_folder = temp_folder / "images"
    image_folder.mkdir(exist_ok=True)
    yaml_path = temp_folder / "meta.yaml"
    yaml_path.write_text(yaml_content.replace("%mapping%", mappings), encoding="utf-8")

    global_id = 0
    label_dict = {}
    for name, index in name_to_id.items():
        folder = root / name
        for image in folder.glob("*.jpg"):
            shutil.copy(str(image), str(image_folder / f"{global_id}.jpg"))
            label_dict[global_id] = index
            global_id += 1
    # write label_dict to csv
    with open(temp_folder / "train.csv", "w", encoding="utf-8") as fp:
        fp.write("name,label\n")
        for key, value in label_dict.items():
            fp.write(f"{key},{value}\n")
    # zip folder
    shutil.make_archive(temp_folder, "zip", temp_folder)
    # remove temp folder
    shutil.rmtree(temp_folder)


if __name__ == "__main__":
    move_images()
