@echo off
chcp 65001 >nul
cd /d "D:\Android\BenDownAndroid"

echo Init git repository...
git init
echo.

echo Adding all files...
git add .
echo.

echo Creating commit...
git commit -m "Initial commit: BenMarkDown阅读器 V0.2"
echo.

echo Adding remote repository...
git remote add origin https://github.com/kitebj/bendown-android.git 2>nul
if errorlevel 1 (
    echo Remote already exists, updating...
    git remote set-url origin https://github.com/kitebj/bendown-android.git
)
echo.

echo ========================================
echo Git setup complete!
echo.
echo Next steps:
echo 1. Check branch name: git branch
echo 2. Push to remote: git push -u origin main
echo    OR: git push -u origin master
echo ========================================
