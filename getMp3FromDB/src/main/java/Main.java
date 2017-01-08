import ml.options.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Created by sic.org on 4/27/2016.
 */
public class Main {
    public static String query = "select ItemID, ItemSound from lessonItem";

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

            getData(conn, src);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (conn != null)
                conn.close();
        }


    }

    public void getData(Connection conn, String src) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            prepStmt = conn.prepareStatement(query);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("ItemID");
                byte[] blob = rs.getBytes("ItemSound");
                System.out.printf("------------------- id =%s", id);
                File file = new File(Paths.get(src).toAbsolutePath().toString() + File.separator + id + ".3gp");
                witeFile(blob, id.toString(), file);
            }

        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        } finally {
            if (prepStmt != null)
                prepStmt.close();

        }

    }

    private void witeFile(byte[] fileStream, String id, File src) {

        try (FileOutputStream fileOutputStream = new FileOutputStream(src)) {
            fileOutputStream.write(fileStream);
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println(e);
        }


    }
}
