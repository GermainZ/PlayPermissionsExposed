package com.germainz.playpermissionsexposed;

public class PermissionData {
    public String permission;
    public String name;
    public CharSequence shortDescription;
    public CharSequence longDescription;

    public PermissionData(String permission, CharSequence name, CharSequence shortDescription, CharSequence longDescription) {
        this.permission = permission;
        this.name = ((String) name).substring(0, 1).toUpperCase() + ((String) name).substring(1);
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }
}
