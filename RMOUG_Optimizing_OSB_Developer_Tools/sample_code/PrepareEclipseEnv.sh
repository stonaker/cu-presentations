# *** This script implicitly assumes that:
#     1. It is being run from the Jenkins job's <workspace> folder.
#     2. The latest /trunk/ has been checked out and is ready to be built.
#     3. Jenkins clears this project workspace at the beginning of each Build.
# ****************************************************************************

# Recursively fix all Windows CRLF problems before doing anything else (regular files only)
find . -type f -exec dos2unix {} \;

# Transform SVN folder structure into an Eclipse-like workspace folder structure
mkdir EclipseWorkspace
mkdir EclipseWorkspace/.metadata
mv ./trunk/* ./EclipseWorkspace/.
# *** Add code to remove any unnecessary files/folders from EclipseWorkspace that might have come from SVN that you don't need.
# *** (For instance, removing OSB Configuration projects for environments other than the current one we are targeting.)

# Read filesystem to build list of all OSB projects and save in variables for use below
cd EclipseWorkspace

# Build a comma-delimited list of files, ultimately to be passed to ConfigExport to build the JAR file.
# This is how we build "all OSB projects in trunk" without needing to maintain a separate list of project names
FOLDERS=`ls -dm */ | tr -d ' ' | tr -d '\n' | tr -d '/' | sed 's/,OSBConfiguration//'`

# Creates a pipe-delimited list of all OSB projects, to save in the OSB Configuration project's .settings folder.
# This step is normally done by the Eclipse GUI, but here we are building the workspace programattically
PROJECTS=`echo $FOLDERS | sed 's/,/|/g'`

# Add project list to the OSBConfiguration project's settings (otherwise the projects won't be "found" during the ConfigExport process of building the JAR file)
sed -e 's/^container.referenced.projects=.*//' ./OSBConfiguration/.settings/com.bea.alsb.core.prefs > ./OSBConfiguration/.settings/com.bea.alsb.core.prefs.tmp
echo "container.referenced.projects=$PROJECTS" >> ./OSBConfiguration/.settings/com.bea.alsb.core.prefs.tmp
mv ./OSBConfiguration/.settings/com.bea.alsb.core.prefs.tmp ./OSBConfiguration/.settings/com.bea.alsb.core.prefs

# Add folder list to a temp properties file for later injection by EnvInject plugin (this will be passed to Ant for further processing during the find/replace portion of the build)
echo "project.list=$FOLDERS" > temppropsfile

# After this script completes, the next step in the Jenkins build flow is to use the EnvInject plugin to read temppropsfile
# and create env variables for Ant in the next step.