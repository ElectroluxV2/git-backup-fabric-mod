# Do not use bang comment here, change default shell by editing `shell` file
echo "Script PWD: $(pwd)"
echo "Arguments passed: $#"
echo "Player count: ${1}"
echo "Tick time: ${2}"
echo "Total levels: ${3}"
echo "Level names: '${*:4:$3}'"

if [ "$1" -le 0 ] ; then
  echo "Skipping backup as no one is online"
  exit 1
fi

if (( $(echo "$2 > 50" |bc -l) )) ; then
  echo "Skipping backup as tick time is too high ($2)"
  exit 2
fi

levelNames=${*:4:$3}
for levelName in "${levelNames[@]}" # Preventing word splitting, because minecraft level names may contain spaces
do
    echo "Adding '$levelName' to commit"
    git add "$levelName"
done

echo "Creating commit"
git commit -m "Automatic backup - $(date)"
git push
echo "Backup done!"
