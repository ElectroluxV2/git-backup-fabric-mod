# Do not use bang comment here, change default shell by editing `shell` file
echo "Script PWD: $(pwd)"
echo "Arguments passed: $#"
echo "Player count: ${1}"
echo "Tick time: ${2}"
echo "Total levels: ${3}"
echo "Level names: '${*:4:$3}'"

echo "Creating git repository"
git init

levelNames=${*:4:$3}
for levelName in "${levelNames[@]}" # Preventing word splitting, because minecraft level names may contain spaces
do
    echo "Adding '$levelName' to repository"
    git add "$levelName"
done

echo "Creating initial commit"

git commit -m "Automatic backup - $(date)"
echo "Initialization done!"
