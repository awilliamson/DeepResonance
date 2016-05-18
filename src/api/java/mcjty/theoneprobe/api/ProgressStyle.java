package mcjty.theoneprobe.api;

/**
 * Style for the progress bar.
 */
public class ProgressStyle {
    private int borderColor = 0xffffffff;
    private int backgroundColor = 0xff000000;
    private int filledColor = 0xffaaaaaa;
    private int alternatefilledColor = 0xffaaaaaa;
    private boolean showText = true;

    private NumberFormat numberFormat = NumberFormat.FULL;

    /// The color that is used for the border of the progress bar
    public ProgressStyle borderColor(int c) {
        borderColor = c;
        return this;
    }

    /// The color that is used for the background of the progress bar
    public ProgressStyle backgroundColor(int c) {
        backgroundColor = c;
        return this;
    }

    /// The color that is used for the filled part of the progress bar
    public ProgressStyle filledColor(int c) {
        filledColor = c;
        return this;
    }

    /// If this is different from the filledColor then the fill color will alternate
    public ProgressStyle alternateFilledColor(int c) {
        alternatefilledColor = c;
        return this;
    }

    /// If true then text is shown inside the progress bar
    public ProgressStyle showText(boolean b) {
        showText = b;
        return this;
    }

    /// The number format to use for the text inside the progress bar
    public ProgressStyle numberFormat(NumberFormat f) {
        numberFormat = f;
        return this;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getFilledColor() {
        return filledColor;
    }

    public int getAlternatefilledColor() {
        return alternatefilledColor;
    }

    public boolean isShowText() {
        return showText;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }
}
