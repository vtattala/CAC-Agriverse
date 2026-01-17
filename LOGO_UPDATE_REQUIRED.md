# Logo Update Required

## New Logo Information

The new "Acre Intelligence" logo needs to be manually added to replace the current logo at:
- `app/src/main/res/drawable/newlogo.png`

### New Logo Details:
- **Source URL**: https://github.com/user-attachments/assets/af6c0c77-9fa4-4ac8-9d77-98b01febe2c5
- **Design**: Green rounded square icon with stars, stripes, and agricultural field design on a dark blue/navy background
- **Current Size**: The existing logo is 1010x1017 PNG - maintain similar dimensions
- **Format**: PNG with transparency (RGBA)

### How to Update:
1. Download the new logo from the URL above
2. Resize to approximately 1010x1010 pixels (keep aspect ratio)
3. Replace `app/src/main/res/drawable/newlogo.png` with the new logo
4. Ensure the image properties (size, format) match the original

### Additional Icon Updates:
The app launcher icons may also need to be updated to reflect the new branding:
- `app/src/main/res/mipmap-*/ic_launcher.png` (multiple density folders)
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`

All other branding elements (text, colors, theme) have been updated in this PR.
