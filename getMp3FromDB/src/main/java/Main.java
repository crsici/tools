import ml.options.Options;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sic.org on 4/27/2016.
 */
public class Main {
    public static  String query = "select ItemID, ItemSound from lessonItem";

    public static void main(String args[]) throws Exception {
        Main main = new Main();
        if (args.length == 0) {
            args = new String[]{"-src=mp3", "-db=target.db"};
        }

        main.process(args);
        System.out.println("Finish update");
    }

    public void process(String args[]) throws Exception {
        Options opt = new Options(args, 0);
        opt.getSet().addOption("src", Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE);
        opt.getSet().addOption("db", Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE);

        if (!opt.check(false, false)) {

            System.out.println(opt.getCheckErrors());
            throw new Exception("Application isn't working correctly!!!");
        }


        String src = opt.getSet().getOption("src").getResultValue(0);
        String db = opt.getSet().getOption("db").getResultValue(0);

        System.out.println(" --- Get database from: " + Paths.get(db).toAbsolutePath().toString());
        File database = new File(Paths.get(db).toAbsolutePath().toString());
        System.out.println(" --- Get get mp3 folder from: " + Paths.get(src).toAbsolutePath().toString());
        File mp3Resouce = new File(Paths.get(src).toAbsolutePath().toString());

        if (!(database.isFile() && mp3Resouce.isDirectory())) {
            throw new IllegalArgumentException("Invalid src and db URL");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
            conn.setAutoCommit(false);

            List<File> sortedMp3 = Arrays.asList(mp3Resouce.listFiles());
            Collections.sort(sortedMp3, (f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();

                        return Integer.valueOf(
                                name1.substring(0, name1.indexOf('.'))
                        ).compareTo(
                                Integer.valueOf(
                                        name2.substring(0, name2.indexOf('.')
                                        )                                )
                        );
                    }
            );
            for (File fileEntry : sortedMp3) {
                overrideDB(conn, fileEntry, database, fileEntry.getName().substring(0, fileEntry.getName().indexOf('.')));
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (conn != null)
                conn.close();
        }


    }

    public void overrideDB(Connection conn, File sound, File db, String itemId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        PreparedStatement prepStmt = null;
        System.out.printf("------------------- update lessonItem set ItemSound=%s where ItemID=%d \n", sound.getName(), Integer.parseInt(itemId));
        try {
            prepStmt = conn.prepareStatement("update lessonItem set ItemSound=? where ItemID=?");
            prepStmt.setInt(2, Integer.parseInt(itemId));
            prepStmt.setBytes(1, getByteArrayFromFile(sound));
            int count = prepStmt.executeUpdate();
            conn.commit();
            if (count != 1) {
                throw new Exception("Can't find item sound ID: " + Integer.parseInt(itemId));
            }

        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        } finally {
            if (prepStmt != null)
                prepStmt.close();

        }

    }

    public void getData(Connection conn, File sound, File db, String itemId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        System.out.printf("------------------- update lessonItem set ItemSound=%s where ItemID=%d \n", sound.getName(), Integer.parseInt(itemId));
        try {
            prepStmt = conn.prepareStatement(query);
            rs = prepStmt.executeQuery();
            while (rs.next()){
                Blob blob = rs.getBlob("ItemSound");
            }

        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        } finally {
            if (prepStmt != null)
                prepStmt.close();

        }

    }

    private byte[] getByteArrayFromFile(File imgFile) {
        byte[] result = null;
        FileInputStream fileInStr = null;
        try {
            fileInStr = new FileInputStream(imgFile);
            long imageSize = imgFile.length();

            if (imageSize > Integer.MAX_VALUE) {
                return null;    //image is too large
            }

            if (imageSize > 0) {
                result = new byte[(int) imageSize];
                fileInStr.read(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInStr.close();
                imgFile.delete();
            } catch (Exception e) {
            }
        }
        return result;
    }
}
