package com.germainz.playpermissionsexposed;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedMod implements IXposedHookLoadPackage {
    protected static final HashMap<String, List<String>> PERMISSION_BUCKETS = new HashMap<String, List<String>>();

    static {
        PERMISSION_BUCKETS.put("ic_perm_in_app_purchases", Arrays.asList(
                "com.android.vending.BILLING"
        ));
        PERMISSION_BUCKETS.put("ic_perm_camera_microphone", Arrays.asList(
                "android.permission.CAMERA", "android.permission.RECORD_AUDIO",
                "android.permission.RECORD_VIDEO"
        ));
        PERMISSION_BUCKETS.put("ic_perm_cal", Arrays.asList(
                "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS",
                "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR"
        ));
        PERMISSION_BUCKETS.put("ic_perm_history", Arrays.asList(
                "android.permission.READ_LOGS", "android.permission.GET_TASKS", "android.permission.DUMP",
                "com.android.browser.permission.READ_HISTORY_BOOKMARKS"
        ));
        PERMISSION_BUCKETS.put("ic_perm_deviceid", Arrays.asList(
                "android.permission.READ_PHONE_STATE"
        ));
        PERMISSION_BUCKETS.put("ic_perm_identity", Arrays.asList(
                "android.permission.GET_ACCOUNTS", "android.permission.MANAGE_ACCOUNTS",
                "android.permission.READ_PROFILE", "android.permission.WRITE_PROFILE"
        ));
        PERMISSION_BUCKETS.put("ic_perm_unknown", Arrays.asList(
                "android.permission.ACCESS_MOCK_LOCATION", "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCOUNT_MANAGER", "android.permission.AUTHENTICATE_ACCOUNTS",
                "android.permission.BATTERY_STATS", "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN", "android.permission.BROADCAST_STICKY",
                "android.permission.CHANGE_CONFIGURATION", "android.permission.CHANGE_NETWORK_STATE",
                "android.permission.CHANGE_WIFI_MULTICAST_STATE", "android.permission.CHANGE_WIFI_STATE",
                "android.permission.CHANGE_WIMAX_STATE", "android.permission.CLEAR_APP_CACHE",
                "android.permission.DISABLE_KEYGUARD", "android.permission.EXPAND_STATUS_BAR",
                "android.permission.FLASHLIGHT", "android.permission.GET_PACKAGE_SIZE", "android.permission.INTERNET",
                "android.permission.KILL_BACKGROUND_PROCESSES", "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.NFC", "android.permission.PERSISTENT_ACTIVITY",
                "android.permission.READ_SYNC_SETTINGS", "android.permission.READ_USER_DICTIONARY",
                "android.permission.RECEIVE_BOOT_COMPLETED", "android.permission.REORDER_TASKS",
                "android.permission.SERIAL_PORT", "android.permission.SET_ALWAYS_FINISH",
                "android.permission.SET_ANIMATION_SCALE", "android.permission.SET_DEBUG_APP",
                "android.permission.SET_PREFERRED_APPLICATIONS", "android.permission.SET_PROCESS_LIMIT",
                "android.permission.SET_TIME_ZONE", "android.permission.SET_WALLPAPER",
                "android.permission.SIGNAL_PERSISTENT_PROCESSES", "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.USE_CREDENTIALS", "android.permission.USE_SIP", "android.permission.VIBRATE",
                "android.permission.WAKE_LOCK", "android.permission.WRITE_SETTINGS",
                "android.permission.WRITE_SYNC_SETTINGS", "android.permission.WRITE_USER_DICTIONARY",
                "com.android.alarm.permission.SET_ALARM", "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS",
                "com.android.launcher.permission.INSTALL_SHORTCUT",
                "com.android.launcher.permission.UNINSTALL_SHORTCUT", "com.android.vending.CHECK_LICENSE",
                "com.google.android.providers.gsf.permission.READ_GSERVICES"
        ));
        PERMISSION_BUCKETS.put("ic_perm_location", Arrays.asList(
                "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS", "android.permission.ACCESS_GPS"
        ));
        PERMISSION_BUCKETS.put("ic_perm_phone", Arrays.asList(
                "android.permission.CALL_PHONE", "android.permission.WRITE_CALL_LOG",
                "android.permission.READ_CALL_LOG", "android.permission.CALL_PRIVILEGED",
                "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.MODIFY_PHONE_STATE"
        ));
        PERMISSION_BUCKETS.put("ic_perm_media", Arrays.asList(
                "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MOUNT_FORMAT_FILESYSTEMS", "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
        ));
        PERMISSION_BUCKETS.put("ic_perm_data_settings", Arrays.asList(
                "android.permission.WRITE_APN_SETTINGS"
        ));
        PERMISSION_BUCKETS.put("ic_perm_messaging", Arrays.asList(
                "android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.WRITE_SMS",
                "android.permission.SEND_SMS", "android.permission.RECEIVE_MMS",
                "android.permission.RECEIVE_WAP_PUSH"
        ));
        PERMISSION_BUCKETS.put("ic_perm_scan_wifi", Arrays.asList(
                "android.permission.ACCESS_WIFI_STATE"
        ));
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.vending"))
            return;

        findAndHookMethod("com.google.android.finsky.utils.PermissionsBucketer", lpparam.classLoader,
                "getPermissionBuckets",
                String[].class, Set.class, boolean.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        String[] np = (String[]) param.args[0];
                        List<String> newPermissions;
                        if (np != null)
                            newPermissions = new ArrayList<String>(Arrays.asList(np));
                        else
                            newPermissions = new ArrayList<String>();

                        Set<String> op = (Set<String>) param.args[1];
                        List<String> oldPermissions;
                        if (op != null)
                            oldPermissions = new ArrayList<String>(op);
                        else
                            oldPermissions = new ArrayList<String>();

                        newPermissions.removeAll(oldPermissions);

                        boolean hasnewPermissions = (newPermissions.size() > 0);

                        Object permissionData = param.getResult();
                        setBooleanField(permissionData, "mForcePermissionPrompt", hasnewPermissions);

                        Context context = AndroidAppHelper.currentApplication();
                        PackageManager packageManager = context.getPackageManager();
                        ArrayList<PermissionData> newPermissionsData = new ArrayList<PermissionData>();
                        ArrayList<PermissionData> oldPermissionsData = new ArrayList<PermissionData>();
                        for (String permission : newPermissions) {
                            CharSequence longDescription;
                            CharSequence name;
                            try {
                                PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission,
                                        PackageManager.GET_META_DATA);
                                longDescription = permissionInfo.loadDescription(packageManager);
                                name = permissionInfo.loadLabel(packageManager);
                            } catch (PackageManager.NameNotFoundException ignored) {
                                longDescription = null;
                                name = permission;
                            }
                            CharSequence shortDescription = null;
                            newPermissionsData.add(new PermissionData(permission, name, shortDescription,
                                    longDescription));
                        }
                        for (String permission : oldPermissions) {
                            CharSequence name;
                            CharSequence longDescription;
                            try {
                                PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission,
                                        PackageManager.GET_META_DATA);
                                longDescription = permissionInfo.loadDescription(packageManager);
                                name = permissionInfo.loadLabel(packageManager);
                            } catch (PackageManager.NameNotFoundException e) {
                                longDescription = null;
                                name = permission;
                            }
                            CharSequence shortDescription = null;
                            oldPermissionsData.add(new PermissionData(permission, name, shortDescription,
                                    longDescription));
                        }

                        setAdditionalInstanceField(permissionData, "newPermissionsData", newPermissionsData);
                        setAdditionalInstanceField(permissionData, "oldPermissionsData", oldPermissionsData);
                        setAdditionalInstanceField(permissionData, "adapterSize",
                                ((newPermissionsData.size() == 0) ? 1 : newPermissions.size()) + (
                                        (oldPermissionsData.size() == 0) ? 0 : 1)
                        );
                        param.setResult(permissionData);
                    }
                }
        );

        findAndHookMethod("com.google.android.finsky.layout.AppPermissionAdapter", lpparam.classLoader,
                "showTheNoPermissionMessage", XC_MethodReplacement.returnConstant(false));

        findAndHookMethod("com.google.android.finsky.layout.AppPermissionAdapter", lpparam.classLoader,
                "getCount", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
                        Object mData = getObjectField(param.thisObject, "mData");
                        int count = (Integer) getAdditionalInstanceField(mData, "adapterSize");
                        return count;
                    }
                }
        );

        findAndHookMethod("com.google.android.finsky.layout.AppPermissionAdapter", lpparam.classLoader,
                "getView", int.class, View.class, ViewGroup.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
                        Object mData = getObjectField(param.thisObject, "mData");
                        ViewGroup viewGroup = (ViewGroup) param.args[2];
                        Context context = (Context) getObjectField(param.thisObject, "mContext");
                        LayoutInflater layoutInflater = (LayoutInflater) getObjectField(param.thisObject,
                                "mLayoutInflater");
                        Resources res = context.getResources();

                        ArrayList<PermissionData> newPermissionsData = (ArrayList<PermissionData>)
                                getAdditionalInstanceField(mData, "newPermissionsData");
                        ArrayList<PermissionData> oldPermissionsData = (ArrayList<PermissionData>)
                                getAdditionalInstanceField(mData, "oldPermissionsData");

                        int index = (Integer) param.args[0];
                        if (index < newPermissionsData.size()) {
                            return getPermissionView(layoutInflater, viewGroup, res, newPermissionsData.get(index));
                        } else {
                            if (index == 0) {
                                TextView textView = (TextView) layoutInflater.inflate(
                                        getLayoutRes("no_permissions_required", res), viewGroup, false);
                                textView.setText(Html.fromHtml(res.getString(getStringRes("no_new_permissions", res),
                                        getOwnString(context, R.string.this_application))));
                                return textView;
                            } else {
                                return getExistingPermissionView(layoutInflater, viewGroup, res, oldPermissionsData);
                            }
                        }
                    }
                }
        );

    }

    private View getPermissionView(LayoutInflater layoutInflater, ViewGroup viewGroup,
                                   final Resources res, PermissionData permissionData) {
        View view = layoutInflater.inflate(getLayoutRes("permission_row", res), viewGroup,
                false);
        TextView headerTextView = (TextView) view.findViewById(getIdRes("header", res));
        final TextView contentTextView = (TextView) view.findViewById(getIdRes("content", res));
        final ImageView expanderIcon = (ImageView) view.findViewById(getIdRes("expander_icon", res));
        ImageView bucketIcon = (ImageView) view.findViewById(getIdRes("bucket_icon", res));

        headerTextView.setText(permissionData.name);
        contentTextView.setText(permissionData.longDescription == null ?
                getOwnString(AndroidAppHelper.currentApplication(), R.string.no_description) :
                permissionData.longDescription);
        contentTextView.setVisibility(View.GONE);
        bucketIcon.setImageResource(getBucketIconRes(permissionData.permission, res));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = (contentTextView.getVisibility() == View.GONE);
                if (expanded) {
                    expanderIcon.setImageResource(getDrawableRes("ic_more_arrow_up", res));
                    contentTextView.setVisibility(View.VISIBLE);
                } else {
                    expanderIcon.setImageResource(getDrawableRes("ic_more_arrow_down", res));
                    contentTextView.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    private View getExistingPermissionView(LayoutInflater layoutInflater, ViewGroup viewGroup,
                                           final Resources res, ArrayList<PermissionData> permissionsData) {
        LinearLayout view = (LinearLayout) layoutInflater.inflate(getLayoutRes("existing_permissions_row", res),
                viewGroup, false);
        ImageView bucketIcon = (ImageView) view.findViewById(getIdRes("bucket_icon", res));
        TextView headerText = (TextView) view.findViewById(getIdRes("header", res));
        final ImageView expanderIcon = (ImageView) view.findViewById(getIdRes("expander_icon", res));
        final TextView shortDescription = (TextView) view.findViewById(getIdRes("short_description", res));
        final LinearLayout detailedBuckets = (LinearLayout) view.findViewById(getIdRes("detailed_buckets", res));
        final LinearLayout permissionRow = (LinearLayout) view.findViewById(getIdRes("permission_row", res));

        bucketIcon.setImageResource(getDrawableRes("ic_perm_check", res));
        headerText.setText(getStringRes("already_has_access_to", res));
        shortDescription.setText(getOwnString(AndroidAppHelper.currentApplication(), R.string.existing_permissions,
                permissionsData.size()));
        permissionRow.setTag(false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = (Boolean) permissionRow.getTag();
                if (!expanded) {
                    expanderIcon.setImageResource(getDrawableRes("ic_more_arrow_up", res));
                    detailedBuckets.setVisibility(View.VISIBLE);
                    shortDescription.setVisibility(View.GONE);
                } else {
                    expanderIcon.setImageResource(getDrawableRes("ic_more_arrow_down", res));
                    detailedBuckets.setVisibility(View.GONE);
                    shortDescription.setVisibility(View.VISIBLE);
                }
                permissionRow.setTag(!expanded);
            }
        });

        for (PermissionData permissionData : permissionsData) {
            View permissionView = getPermissionView(layoutInflater, viewGroup, res, permissionData);
            permissionView.setOnClickListener(null);
            permissionView.setClickable(false);
            permissionView.findViewById(getIdRes("expander_icon", res)).setVisibility(View.GONE);
            permissionView.findViewById(getIdRes("content", res)).setVisibility(View.VISIBLE);
            detailedBuckets.addView(permissionView);
        }

        return view;
    }

    private int getResource(String name, String type, Resources res) {
        return res.getIdentifier(name, type, "com.android.vending");
    }

    private int getIdRes(String name, Resources res) {
        return getResource(name, "id", res);
    }

    private int getDrawableRes(String name, Resources res) {
        return getResource(name, "drawable", res);
    }

    private int getStringRes(String name, Resources res) {
        return getResource(name, "string", res);
    }

    private int getLayoutRes(String name, Resources res) {
        return getResource(name, "layout", res);
    }

    private int getBucketIconRes(String permission, Resources res) {
        for (HashMap.Entry<String, List<String>> entry : PERMISSION_BUCKETS.entrySet()) {
            String icon = entry.getKey();
            List<String> permissions = entry.getValue();
            if (permissions.contains(permission))
                return getDrawableRes(icon, res);
        }
        return getDrawableRes("ic_perm_unknown", res);
    }

    private static String getOwnString(Context context, int resId, Object... formatArgs) {
        Context packageContext;
        try {
            packageContext = context.createPackageContext("com.germainz.playpermissionsexposed",
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        if (formatArgs.length > 0)
            return packageContext.getString(resId, formatArgs);
        else
            return packageContext.getString(resId);
    }
}