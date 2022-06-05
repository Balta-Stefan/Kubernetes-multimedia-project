package pisio.worker.services;

import pisio.common.model.messages.ExtractAudioMessage;

public interface AudioService
{
    boolean extractAudio(ExtractAudioMessage msg);
}
