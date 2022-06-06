package pisio.common.model.enums;


public enum Resolutions
{
    RES480p(720, 480),
    RESHD(1280, 720),
    RESFULLHD(1920, 1080),
    RES2K(2560, 1440),
    RES4K(3840, 2160);

    private final int width, height;

    Resolutions(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
