Introduction
============
The newer Play Store versions bring the following changes in regards to permissions:
* Permissions are now shown under vague categories when you press the "Update"/"Install" buttons. 42 permissions are also hidden from this screen (list below). To see almost all individual permissions, you can scroll down and tap "View details". Also note that unknown permissions (those defined by apps) are usually hidden in both screens.
* Auto-updates only check for new categories, not new permissions.
For more info, I'd recommend you read [this](http://www.xda-developers.com/android/play-store-permissions-change-opens-door-to-rogue-apps/") article by pulser_g2.

Purpose of this module
======================
This module aims to fix this problem for users who care about permissions. It'll do the following:
* Make the Play Store show you all of the app's permissions.
* Require you to manually update apps with new permissions (regardless of the category).
It more or less restores the old behavior.

Download
========
http://repo.xposed.info/module/com.germainz.playpermissionsexposed

XDA Thread
==========
http://forum.xda-developers.com/xposed/modules/playpermissionsexposed-fix-play-store-t2783076
