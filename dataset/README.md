# FedEYE-Dataset-Toolkit

Dataset toolkit for FedEYE.

The EDDL dataset is available at [https://github.com/CRazorback/The-SUSTech-SYSU-dataset-for-automatically-segmenting-and-classifying-corneal-ulcers](https://github.com/CRazorback/The-SUSTech-SYSU-dataset-for-automatically-segmenting-and-classifying-corneal-ulcers) and the OCT17 dataset is available at [https://data.mendeley.com/datasets/rscbjbr9sj/2](https://data.mendeley.com/datasets/rscbjbr9sj/2)

## Usage

1. Download the dataset.
2. Modify the `dataset_root` in `eddl.py` and `oct17.py` to the path of the dataset.
3. Change the parameters such as `num_clients`, `iid` and `alpha` in `eddl.py` and `oct17.py` to generate the dataset you want.
4. Run `python eddl.py` and `python oct17.py` to generate the dataset.
5. The generated dataset zip files will be saved in the `dataset_root/export` folder.
6. Upload the generated dataset zip files to the FedEYE platform.

## License

Apache License 2.0