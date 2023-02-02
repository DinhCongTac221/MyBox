package mara.mybox.value;

import java.awt.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.fxml.style.StyleData.StyleColor;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class Colors {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static Color color(StyleColor inStyle, boolean dark) {
        StyleColor style = inStyle;
        String color;
        if (style == null) {
            style = StyleColor.Red;
        }
        switch (style) {
            case Blue:
                color = dark ? "0x003472FF" : "0xE3F9FDFF";
                break;
            case LightBlue:
                color = dark ? "0x4C8DAEFF" : "0xD6ECF0FF";
                break;
            case Pink:
                color = dark ? "0xFF0097FF" : "0xEDD1D8FF";
                break;
            case Orange:
                color = dark ? "0xCA6924FF" : "0xFFF2DFFF";
                break;
            case Green:
                color = dark ? "0x0D3928FF" : "0xE0F0E9FF";
                break;
            case Customize:
                color = dark ? AppVariables.CustomizeColorDark : AppVariables.CustomizeColorLight;
                break;
            default:
                color = dark ? "0xC32136FF" : "0xFBD5CFFF";
        }
        return ColorConvertTools.rgba2color(color);
    }

}
