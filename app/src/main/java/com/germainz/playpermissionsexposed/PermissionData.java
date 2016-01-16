package com.germainz.playpermissionsexposed;

public class PermissionData {
    public String permission;
    public String name;
    public CharSequence description;

    public PermissionData(String permission, CharSequence name, CharSequence description) {
        this.permission = permission;
        if (name == null || name.length() <= 0)
            this.name = permission;
        else
            this.name = ((String) name).substring(0, 1).toUpperCase() + ((String) name).substring(1);
        this.description = description;
    }
}
