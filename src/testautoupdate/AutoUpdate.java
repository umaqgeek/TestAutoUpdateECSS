/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testautoupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class AutoUpdate {

    public void update() throws IOException {
        String result = null, 
                line = null, 
                path = "G:/CSS/", 
                propertiesLocation = "data/version.properties";
        String ipaddress = "http://localhost:8080/updateServer/";
        HttpURLConnection connection = (HttpURLConnection) new URL(ipaddress + "testUpdate.jsp").openConnection();
        int intNotUpdate = 0;

        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setInstanceFollowRedirects(false);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                result = line;
            }
            reader.close();

            String[] strServerVer = result.split("%");
            int iVersion = strServerVer.length;

            String[] strUpdateClientVer = new String[iVersion];

            File fileProp = new File(propertiesLocation);
            FileInputStream fileInput = new FileInputStream(fileProp);
            Properties prop = new Properties();
            prop.load(fileInput);
            fileInput.close();

            List<FileProperties> listFp = new ArrayList<>();

            //System.out.println(String.valueOf(iVersion));
            for (int i = 0; i < iVersion; i++) {
                int countFile = (i + 1);
                String strCount = String.valueOf(countFile);
                String[] strClient = prop.getProperty("file" + strCount).split("%");

                String strClientVer = strClient[0];
                String fileName = strClient[1];

                int client = Integer.parseInt(strClientVer); // current version
                int server = Integer.parseInt(strServerVer[i]); // server version

                //System.out.println(strClientVer);
                strUpdateClientVer[i] = strClientVer + "%" + fileName;

                if (client < server) {
                    String name_jar = fileName;
                    HttpURLConnection connection_jar = (HttpURLConnection) new URL(ipaddress + name_jar).openConnection();

                    connection_jar.setRequestMethod("HEAD");
                    connection_jar.setDoOutput(true);
                    connection_jar.setDoInput(true);
                    connection_jar.setDoOutput(true);
                    connection_jar.setUseCaches(false);
                    connection_jar.setDefaultUseCaches(false);
                    connection_jar.setInstanceFollowRedirects(false);

                    if (connection_jar.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        File file = new File(name_jar);

                        if (file.exists()) {
                            file.delete();
                        }

                        FileProperties fp = new FileProperties();
                        fp.setSource(ipaddress + name_jar);
                        fp.setTarget(path + name_jar);

                        listFp.add(fp);

                        strUpdateClientVer[i] = strServerVer[i] + "%" + fileName;

                        //Files.copy(new URL(ipaddress + name_jar).openStream(), Paths.get(path + name_jar));
                        //Runtime.getRuntime().exec(new String[] {"cmd", "/c", "ping 127.0.0.1 -n 3 -w 1000 && javaw -jar \"" + path + name_jar + "\""});
                        //System.exit(0);
                    } else {
                        System.out.println("Update file not found on the update server");
                    }
                } else {
                    System.out.println("No update");
                    intNotUpdate++;
                }
            }

            if (!listFp.isEmpty()) {
                FileCopierUtility fcp = new FileCopierUtility(listFp);
                fcp.setVisible(true);
                fcp.runDownload();

                writeProperties(propertiesLocation, strUpdateClientVer);
            }
            System.out.println(intNotUpdate+propertiesLocation.length());
            if(intNotUpdate == strUpdateClientVer.length)
            {
                JOptionPane.showMessageDialog(null, "No update");
            }

        } else {
            System.out.println("Unable to connect to update server");
        }
    }

    private void writeProperties(String propertiesLocation, String[] strUpdateClientVer) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream(propertiesLocation);

            for (int i = 0; i < strUpdateClientVer.length; i++) {
                int fileNo = i + 1;
                String sFileNo = String.valueOf(fileNo);
                // set the properties value
                prop.setProperty("file" + sFileNo, strUpdateClientVer[i]);
            }

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
