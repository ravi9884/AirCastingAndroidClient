package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.sensor.BluetoothSocketReader;

import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BioharnessDataReader implements BluetoothSocketReader
{
  private final BluetoothSocket socket;
  private EventBus eventBus;

  boolean active;

  BioharnessPacketReader reader;
  ByteArrayOutputStream bos = new ByteArrayOutputStream();

  public BioharnessDataReader(BluetoothSocket socket, EventBus eventBus)
  {
    this.socket = socket;
    this.reader = new BioharnessPacketReader(eventBus);
  }

  @Override
  public void read() throws IOException
  {
    active = true;

    InputStream stream = socket.getInputStream();
    byte[] readBuffer = new byte[4096];

    while (active)
    {
      int bytesRead = stream.read(readBuffer);
      if (bytesRead > 0)
      {
        bos.write(readBuffer, 0, bytesRead);
        int processed = reader.tryReading(bos);
        if(processed > 0)
        {
          byte[] bytes = bos.toByteArray();
          bos = new ByteArrayOutputStream();
          bos.write(bytes, processed, bytes.length - processed);
        }
      }
      else
      {
        sleepFor(100);
      }
    }
  }

  @Override
  public void setEventBus(EventBus eventBus)
  {
    this.eventBus = eventBus;
  }

  private void sleepFor(long sleepTime)
   {
     try
     {
       Thread.sleep(sleepTime);
     }
     catch (InterruptedException ignore)
     {

     }
   }
}