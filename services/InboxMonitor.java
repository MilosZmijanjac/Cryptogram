package services;

import controllers.ChatController;
import controllers.HomeController;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;

public  class InboxMonitor implements Runnable {
    boolean running;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private Path directoryPath;

    public InboxMonitor(String directoryPath ){
        this.directoryPath=Path.of(directoryPath);
    }

    public void run() {
        try {
            setRunning(true);

            WatchService watchService = directoryPath.getFileSystem().newWatchService();
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);


            while (isRunning()) {

                WatchKey watchKey = watchService.take();


                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    takeActionOnChangeEvent(event);
                }

                if (!watchKey.reset()) {
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private synchronized void takeActionOnChangeEvent(WatchEvent<?> event) throws InterruptedException, IOException {
        Thread.sleep(350);
        WatchEvent.Kind<?> kind = event.kind();

        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path entryCreated = (Path) event.context();
            try {
                BufferedReader in = new BufferedReader(new FileReader(directoryPath.toString() + File.separator + entryCreated));
                String tmp = in.readLine();
                    if(tmp.contains("request")) {
                        HomeController.chatRequest(tmp);
                    }else if(tmp.contains("<type>reply</type><content>yes</content>")){
                        ChatController.inChat=true;
                        Platform.runLater(HomeController::showChat);
                    }else if(tmp.contains("termination")){ChatController.inChat=false;
                        Platform.runLater(ChatController::endChat);
                    }else{
                        ChatController.observableList.add(tmp);
                    }
                    in.close();
                    //System.out.println(tmp+" >>>> "+(event.context()).toString()+" >>>"+directoryPath.toString());

                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(directoryPath.toString() + File.separator + entryCreated)));
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //new File(directoryPath.toString() + File.separator + entryCreated).delete();
            Files.deleteIfExists(Paths.get(directoryPath.toString() + File.separator + entryCreated));


        }
    }




}
