package pisio.worker.services.impl;

import org.springframework.stereotype.Service;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.worker.services.AudioService;

@Service
public class AudioServiceImpl implements AudioService
{
    @Override
    public boolean extractAudio(ExtractAudioMessage msg)
    {
        return false;
    }
}
