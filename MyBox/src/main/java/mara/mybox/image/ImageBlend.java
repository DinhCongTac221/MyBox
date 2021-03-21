package mara.mybox.image;

import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.PixelBlend.ImagesBlendMode;

/**
 * @Author Mara
 * @CreateDate 2018-10-31 20:03:32
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageBlend {

    protected ImagesRelativeLocation relativeLocation;
    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected boolean intersectOnly;
    protected int x, y;
    protected BufferedImage foreImage, backImage;

    public enum ImagesRelativeLocation {
        Foreground_In_Background,
        Background_In_Foreground
    }

    public ImageBlend() {

    }

    public ImageBlend(BufferedImage foreImage, BufferedImage backImage,
            ImagesRelativeLocation relativeLocation, ImagesBlendMode blendMode,
            float opacity, boolean intersectOnly, int x, int y) {
        this.foreImage = foreImage;
        this.backImage = backImage;
        this.relativeLocation = relativeLocation;
        this.blendMode = blendMode;
        this.opacity = opacity;
        this.intersectOnly = intersectOnly;
        this.x = x;
        this.y = y;
    }

    public BufferedImage operate() {
        try {
            if (foreImage == null || backImage == null
                    || relativeLocation == null || blendMode == null) {
                return null;
            }
            switch (relativeLocation) {
                case Foreground_In_Background:
                    if (intersectOnly) {
                        return blendImagesFinBIntrsectOnly(foreImage, backImage, x, y, blendMode, opacity);
                    } else {
                        return blendImagesFinB(foreImage, backImage, x, y, blendMode, opacity);
                    }
                case Background_In_Foreground:
                    if (intersectOnly) {
                        return blendImagesBinFIntrsectOnly(foreImage, backImage, x, y, blendMode, opacity);
                    } else {
                        return blendImagesBinF(foreImage, backImage, x, y, blendMode, opacity);
                    }
                default:
                    return foreImage;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return foreImage;
        }
    }

    public static BufferedImage blendImages(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity) {
        return ImageBlend.blendImages(foreImage, backImage, x, y, blendMode, opacity, false);
    }

    public static BufferedImage blendImages(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = new DoubleRectangle(x, y,
                    x + foreImage.getWidth() - 1, y + foreImage.getHeight() - 1);
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            PixelBlend colorBlend = PixelBlend.newColorBlend(blendMode)
                    .setBlendMode(blendMode).setOrderReversed(orderReversed).setOpacity(opacity);
            for (int j = 0; j < backImage.getHeight(); ++j) {
                for (int i = 0; i < backImage.getWidth(); ++i) {
                    int backPixel = backImage.getRGB(i, j);
                    if (rect.include(i, j)) {
                        int forePixel = foreImage.getRGB(i - x, j - y);
                        target.setRGB(i, j, colorBlend.blend(forePixel, backPixel));
                    } else {
                        target.setRGB(i, j, backPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImages(BufferedImage foreImage, BufferedImage backImage,
            ImagesRelativeLocation location, int x, int y,
            boolean intersectOnly, ImagesBlendMode blendMode, float opacity) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            switch (location) {
                case Foreground_In_Background:
                    if (intersectOnly) {
                        return blendImagesFinBIntrsectOnly(foreImage, backImage, x, y, blendMode, opacity);
                    } else {
                        return blendImagesFinB(foreImage, backImage, x, y, blendMode, opacity);
                    }
                case Background_In_Foreground:
                    if (intersectOnly) {
                        return blendImagesBinFIntrsectOnly(foreImage, backImage, x, y, blendMode, opacity);
                    } else {
                        return blendImagesBinF(foreImage, backImage, x, y, blendMode, opacity);
                    }
                default:
                    return foreImage;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return foreImage;
        }
    }

    public static BufferedImage blendImagesFinB(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            for (int j = 0; j < backImage.getHeight(); ++j) {
                for (int i = 0; i < backImage.getWidth(); ++i) {
                    target.setRGB(i, j, backImage.getRGB(i, j));
                }
            }
            int areaWidth = Math.min(backImage.getWidth() - x, foreImage.getWidth());
            int areaHeight = Math.min(backImage.getHeight() - y, foreImage.getHeight());
            PixelBlend colorBlend = PixelBlend.newColorBlend(blendMode)
                    .setBlendMode(blendMode).setOpacity(opacity);
            for (int j = 0; j < areaHeight; ++j) {
                for (int i = 0; i < areaWidth; ++i) {
                    int pixelFore = foreImage.getRGB(i, j);
                    int pixelBack = backImage.getRGB(i + x, j + y);
                    target.setRGB(i + x, j + y, colorBlend.blend(pixelFore, pixelBack));
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesFinBIntrsectOnly(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int areaWidth = Math.min(backImage.getWidth() - x, foreImage.getWidth());
            int areaHeight = Math.min(backImage.getHeight() - y, foreImage.getHeight());
            BufferedImage target = new BufferedImage(areaWidth, areaHeight, imageType);
            PixelBlend colorBlend = PixelBlend.newColorBlend(blendMode)
                    .setBlendMode(blendMode).setOpacity(opacity);
            for (int j = 0; j < areaHeight; ++j) {
                for (int i = 0; i < areaWidth; ++i) {
                    int pixelFore = foreImage.getRGB(i, j);
                    int pixelBack = backImage.getRGB(i + x, j + y);
                    target.setRGB(i, j, colorBlend.blend(pixelFore, pixelBack));
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesBinF(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(foreImage.getWidth(), foreImage.getHeight(), imageType);
            for (int j = 0; j < foreImage.getHeight(); ++j) {
                for (int i = 0; i < foreImage.getWidth(); ++i) {
                    target.setRGB(i, j, foreImage.getRGB(i, j));
                }
            }
            int areaWidth = Math.min(foreImage.getWidth() - x, backImage.getWidth());
            int areaHeight = Math.min(foreImage.getHeight() - y, backImage.getHeight());
            PixelBlend colorBlend = PixelBlend.newColorBlend(blendMode)
                    .setBlendMode(blendMode).setOpacity(opacity);
            for (int j = 0; j < areaHeight; ++j) {
                for (int i = 0; i < areaWidth; ++i) {
                    int pixelFore = foreImage.getRGB(i + x, j + y);
                    int pixelBack = backImage.getRGB(i, j);
                    target.setRGB(i + x, j + y, colorBlend.blend(pixelFore, pixelBack));
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesBinFIntrsectOnly(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int areaWidth = Math.min(foreImage.getWidth() - x, backImage.getWidth());
            int areaHeight = Math.min(foreImage.getHeight() - y, backImage.getHeight());
            BufferedImage target = new BufferedImage(areaWidth, areaHeight, imageType);
            PixelBlend colorBlend = PixelBlend.newColorBlend(blendMode)
                    .setBlendMode(blendMode).setOpacity(opacity);
            for (int j = 0; j < areaHeight; ++j) {
                for (int i = 0; i < areaWidth; ++i) {
                    int pixelFore = foreImage.getRGB(i + x, j + y);
                    int pixelBack = backImage.getRGB(i, j);
                    target.setRGB(i, j, colorBlend.blend(pixelFore, pixelBack));
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public ImagesRelativeLocation getRelativeLocation() {
        return relativeLocation;
    }

    public void setRelativeLocation(ImagesRelativeLocation relativeLocation) {
        this.relativeLocation = relativeLocation;
    }

    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isIntersectOnly() {
        return intersectOnly;
    }

    public void setIntersectOnly(boolean intersectOnly) {
        this.intersectOnly = intersectOnly;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BufferedImage getForeImage() {
        return foreImage;
    }

    public void setForeImage(BufferedImage foreImage) {
        this.foreImage = foreImage;
    }

    public BufferedImage getBackImage() {
        return backImage;
    }

    public void setBackImage(BufferedImage backImage) {
        this.backImage = backImage;
    }

}
