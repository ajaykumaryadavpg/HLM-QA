## How to Contribute
** Make Sure you are running Java 17 on your machine

1. Clone this repository
2. Create a new branch from develop with your initials e.g. `ss-what-is-your-branch-about`
3. Checkout your branch
4. Install Dependencies and package `mvn clean install -DskipTests=true`
5. Make your changes
6. Write a test in novus-example-test module `package com.tpg.automation.example`
7. Commit and push your changes
8. Create a Github Pull Request 

### Code reviews

All submissions, including submissions by project members, require review. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

### Code Style
- Please follow the fluent code style as followed in the framework, if you have confusions reach out to sidhant.satapathy@3pillarglobal.com 
- Comments should be generally avoided. If the code would not be understood without comments, consider re-writing the code to make it self-explanatory.

### Commit Messages

Commit messages should follow the Semantic Commit Messages format:

```
label: title

description : 
```

1. *label* is one of the following:
    - `fix` - novus bug fixes.
    - `feat` - novus features.
    - `docs` - changes to docs, e.g. `docs(api.md): ..` to change documentation.
2. *title* is a brief summary of changes.
3. *description* should have details of the changes that are done in the PR

### Adding New Dependencies

For all dependencies (both installation and development):
- **Do not add** a dependency if the desired functionality is easily implementable.
- If adding a dependency, it should be well-maintained and trustworthy.