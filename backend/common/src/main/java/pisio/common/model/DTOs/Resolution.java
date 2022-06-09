package pisio.common.model.DTOs;


import lombok.Data;


@Data
public class Resolution
{
    private Integer width;
    private Integer height;

    @Override
    public String toString()
    {
        return "x=" + width + ", y=" + height;
    }

    public boolean isValid()
    {
        if(width != null && width > 0 && height != null && height > 0)
            return true;
        return false;
    }
}
