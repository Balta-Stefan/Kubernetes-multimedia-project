package pisio.common.model.DTOs;


import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class Resolution
{
    @Positive
    private int width;
    @Positive
    private int height;

    @Override
    public String toString()
    {
        return "x=" + width + ", y=" + height;
    }
}
