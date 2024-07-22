![](./src/main/resources/assets/backup-scripts/icon.png)
# What is this?
This mod hooks itself to built in save system and executes scripts:

**Each script is executed in server root directory (next to .jar file) with the following arguments:**
- `1` - player count
- `2` - average tick time (float)
- `3` - server running ("true" or "false")
- `4` - total level names
- `5+` - level name #n (server may run multiple levels at once, level name is equal to directory name)

Each script is by default executed in `/bin/bash`, but you may provide your shell by editing `mods/backup-scripts/shell` file.

## Init script:
**This script is executed only if contains changes since last execution**
To manually force execution without changes delete `.last-init-run` file from `mods/backup-scripts` directory.
```bash
# Do not use Shebang here, change default shell by editing `shell` file
echo "Script PWD: $(pwd)"
echo "Arguments passed: $#"
echo "Player count: ${1}"
echo "AVG Tick time: ${2}"
echo "Server running: ${3}"
echo "Total levels: ${4}"
echo "Level names: '${*:5:$4}'"

echo "Creating git repository"
git init

levelNames=${*:5:$4}
for levelName in "${levelNames[@]}" # Preventing word splitting, because minecraft level names may contain spaces
do
    echo "Adding '$levelName' to repository"
    git add "$levelName"
done

echo "Creating initial commit"

git commit -m "Automatic backup - $(date)"
echo "Initialization done!"
```
Example output:
```
[15:52:21] [Server thread/INFO] (Minecraft) Done (15.286s)! For help, type "help"
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Script PWD: /Users/mateusz.budzisz/git/fabric/git-backup-fabric-mod/run
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Arguments passed: 4
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Player count: 0
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): TPS: 0.0
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Total levels: 1
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Level names: 'world'
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Creating git repository
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Initialized empty Git repository in /Users/mateusz.budzisz/git/fabric/git-backup-fabric-mod/run/.git/
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Adding 'world' to repository
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Creating initial commit
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): [main (root-commit) a142833] Automatic backup - Sun Aug 13 15:52:21 CEST 2023
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  11 files changed, 1 insertion(+)
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/entities/r.-1.-1.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/entities/r.-1.0.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/entities/r.0.-1.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/entities/r.0.0.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/level.dat
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/poi/r.0.0.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/region/r.-1.-1.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/region/r.-1.0.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/region/r.0.-1.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/region/r.0.0.mca
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh):  create mode 100644 world/session.lock
[15:52:21] [Server thread/INFO] (BackupScripts) (init.sh): Initialization done!
```
## OnSave script
**This script is executed at every auto save (Minecraft performs auto save every 5 minutes, use `save-on` to enable), you may use `save-all` to force save.**
Default script contents:
```bash
# Do not use Shebang here, change default shell by editing `shell` file
echo "Script PWD: $(pwd)"
echo "Arguments passed: $#"
echo "Player count: ${1}"
echo "AVG Tick time: ${2}"
echo "Server running: ${3}"
echo "Total levels: ${4}"
echo "Level names: '${*:5:$4}'"

if [ "$1" -le 0 ] && [ "$3" = "true" ] ; then
  echo "Skipping backup as no one is online"
  exit 1
fi

# AVG tick time is float, hence this wierd comparison
if (( $(echo "$2 > 50" |bc -l) )) && [ "$3" = "true" ] ; then
  echo "Skipping backup as avg tick time is too high ($2)"
  exit 2
fi

levelNames=${*:5:$4}
for levelName in "${levelNames[@]}" # Preventing word splitting, because minecraft level names may contain spaces
do
    echo "Adding '$levelName' to commit"
    git add "$levelName"
done

echo "Creating commit"
git commit -m "Automatic backup - $(date)"
git push
echo "Backup done!"
```
Example output:
```
[15:43:50] [Server thread/INFO] (BackupScripts) Running scripts off main thread
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Script PWD: /Users/mateusz.budzisz/git/fabric/git-backup-fabric-mod/run
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Arguments passed: 4
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Player count: 1
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): AVG tick time: 0.1340647
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Total levels: 1
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Level names: 'world with space in name'
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Adding 'world with space in name' to commit
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Creating commit
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): [main 7cd92d5] Automatic backup - Sun Aug 13 15:43:50 CEST 2023
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh):  9 files changed, 0 insertions(+), 0 deletions(-)
[15:43:50] [Thread-13/INFO] (BackupScripts) (on-save.sh): Backup done!
[15:43:50] [Server thread/INFO] (BackupScripts) Shutdown of off main thread
```

## Default scripts use GIT
Which is fine for small worlds (e.g.: up to few gigabytes), but it is recommended to setup more reliable solution as https://github.com/bup/bup or https://github.com/WayneD/rsync
