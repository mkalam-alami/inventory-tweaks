# About the docs

This folder stores the contents of the official website. Each commit made here will be reflected on the documentation thanks to [Read the Docs](http://readthedocs.org).

## How to build the website locally

* Install [Sphinx](http://sphinx.pocoo.org/)
* Open a console
* Set your current directory to `docs/`
* Run: **`sphinx-build . _build`**
* Open `docs/_build/index.html`

## Website update

* Read the docs listens to the `master` branch, so changes from `develop` won't be visible until a release is made.