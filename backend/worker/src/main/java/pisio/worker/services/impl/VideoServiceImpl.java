package pisio.worker.services.impl;

import org.springframework.stereotype.Service;
import pisio.common.model.messages.Transcode;
import pisio.worker.services.VideoService;

@Service
public class VideoServiceImpl implements VideoService
{
    @Override
    public boolean transcode(Transcode msg)
    {
        return false;
    }
}
