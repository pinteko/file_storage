package geekbrains.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Message {

    private final String namePath;
    private final String namePackage;
    private int length;
    private byte[] data;
}
