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
