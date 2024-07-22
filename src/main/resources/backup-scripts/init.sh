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
