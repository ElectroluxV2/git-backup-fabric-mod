# What is this?
This mod hooks itself to built in save system and executes following commands:
- `git add <world name>` for each loaded world
- `git commit -m "<current datetime>"`
- `git pushall` - it is custom command, yo need to define it

Generally minecraft runs auto save every 5 minutes (`save-on` to enable), you may force save by executing `save-all` or simply `stop` server.

# Setup
- Create repository next to world folder `git init`
- Setup custom `git pushall`, see [this](https://stackoverflow.com/a/18674313/7132461) on how to set up it
- Done (this mod has no configuration)