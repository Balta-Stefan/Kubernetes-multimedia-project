package pisio.worker.services;

import pisio.common.model.messages.Transcode;

public interface VideoService
{
    boolean transcode(Transcode msg);
}
