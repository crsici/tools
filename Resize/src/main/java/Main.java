import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by sic.org on 4/24/2016.
 */
public class Main {


    public static void main(String args[]) throws IOException {

        final Main main = new Main();
//        main.resize();
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        Files.lines(Paths.get("Config.txt")).forEach(line -> {
            String[] numbers = line.split(",");
            System.out.println(numbers[0] + "," + numbers[1]);
            try {
                main.resize(Integer.valueOf(numbers[0].trim()), Integer.valueOf(numbers[1].trim()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Finish");
    }


    public void resize(Integer i1, Integer i2) throws IOException {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println(s + File.separator + "result" + File.separator + i1 + "_" + i2);
        File des = new File(s + File.separator + "result" + File.separator + i1 + "_" + i2);
        Thumbnails.of(new File(s + File.separator + "pic.png"))
                .addFilter(new net.coobird.thumbnailator.filters.Canvas(i1, i2, Positions.CENTER, new Color(0,0,0,0)))
                .imageType(BufferedImage.TYPE_INT_ARGB)
                .size(i1, i2)
                .outputQuality(1)
                .toFile(des);
    }
}
