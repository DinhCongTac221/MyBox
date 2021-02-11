package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifViewerController extends ImageViewerController {

    protected Image[] images;
    protected int[] delays;
    protected int currentIndex, currentDelay, fromIndex, toIndex, totalNumber;
    protected double speed;

    @FXML
    protected ComboBox<String> frameBox, speedSelector;
    @FXML
    protected Button pauseButton, extractButton;
    @FXML
    protected HBox operation3Box;
    @FXML
    protected Label promptLabel, commentsLabel;
    @FXML
    protected TextField fromInput, toInput;

    public ImageGifViewerController() {
        baseTitle = AppVariables.message("ImageGifViewer");

        SourceFileType = VisitHistory.FileType.Gif;
        SourcePathType = VisitHistory.FileType.Gif;
        TargetFileType = VisitHistory.FileType.Gif;
        TargetPathType = VisitHistory.FileType.Gif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        needNotRulers = true;
        needNotCoordinates = true;

        sourceExtensionFilter = CommonFxValues.GifExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            operation3Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(fromInput.getText());
                        if (v > 0 && v <= totalNumber) {
                            fromIndex = v;
                            fromInput.setStyle(null);
                        } else {
                            fromInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        fromInput.setStyle(badStyle);
                    }
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(toInput.getText());
                        if (v > 0 && v <= totalNumber && fromIndex <= v) {
                            toIndex = v;
                            toInput.setStyle(null);
                        } else {
                            toInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        toInput.setStyle(badStyle);
                    }
                }
            });

            extractButton.disableProperty().bind(
                    Bindings.isEmpty(fromInput.textProperty())
                            .or(fromInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(toInput.textProperty()))
                            .or(toInput.styleProperty().isEqualTo(badStyle))
            );

            frameBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        showGifFrame(v);
                    } catch (Exception e) {
                    }
                }
            });

            setPauseButton(false);

            speed = 1.0;
            speedSelector.getItems().addAll(Arrays.asList(
                    "1", "1.5", "2", "0.5", "0.8", "1.2", "0.3", "3", "0.1", "5", "0.2", "8"
            ));
            speedSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v <= 0) {
                            speedSelector.getEditor().setStyle(badStyle);
                        } else {
                            speed = v;
                            speedSelector.getEditor().setStyle(null);
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            speedSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadImage(File file, boolean onlyInformation, int inLoadWidth, int inFrameIndex) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            fromInput.setText("");
            toInput.setText("");
            sourceFile = file;
            final String fileName = file.getPath();
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(fileName);
                        if (imageFileInformation == null || imageFileInformation.getImageInformation() == null) {
                            return true;
                        }
                        imageInformation = imageFileInformation.getImageInformation();
                        List<ImageInformation> imagesInformation = imageFileInformation.getImagesInformation();
                        delays = new int[imagesInformation.size()];
                        for (int i = 0; i < imagesInformation.size(); ++i) {
                            Object d = imagesInformation.get(i).getNativeAttribute("delayTime");
                            if (d == null) {
                                delays[i] = 500;
                            } else {
                                delays[i] = Integer.valueOf((String) d);
                            }
                        }
                        if (onlyInformation) {
                            return true;
                        }
                        List<BufferedImage> bimages = ImageFileReaders.readFrames("gif", fileName);
                        if (bimages == null) {
                            return false;
                        }
                        totalNumber = bimages.size();
                        images = new Image[totalNumber];
                        for (int i = 0; i < totalNumber; ++i) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            Image m = SwingFXUtils.toFXImage(bimages.get(i), null);
                            images[i] = m;
                        }
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        if (totalNumber > 0) {
                            image = images[0];
                        }
                        return image != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        afterImageLoaded();
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            if (images == null || images.length == 0) {
                return false;
            }
            showGifImage(1);
            List<String> frames = new ArrayList<>();
            for (int i = 1; i <= images.length; ++i) {
                frames.add(i + "");
            }
            frameBox.getItems().clear();
            frameBox.getItems().addAll(frames);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            fromInput.setText("1");
            toInput.setText(totalNumber + "");
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected void setPauseButton(boolean setAsPaused) {
        if (setAsPaused) {
            ControlStyle.setNameIcon(pauseButton, message("Continue"), "iconPlay.png");
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("Paused");
        } else {
            ControlStyle.setNameIcon(pauseButton, message("Pause"), "iconPause.png");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pauseButton.setUserData("Playing");
        }
        pauseButton.applyCss();
    }

    @FXML
    public void pauseAction() {
        try {

            if (pauseButton.getUserData().equals("Playing")) {
                showGifFrame(currentIndex);

            } else if (pauseButton.getUserData().equals("Paused")) {
                showGifImage(currentIndex);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        try {
            showGifFrame(currentIndex - 2);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        try {
            showGifFrame(currentIndex);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void extractAction() {
        try {
            if (sourceFile == null || images.length == 0
                    || totalNumber <= 0 || fromIndex > toIndex) {
                return;
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    FileTools.getFilePrefix(sourceFile.getName()),
                    CommonFxValues.ImageExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private List<String> filenames;

                    @Override
                    protected boolean handle() {
                        filenames = ImageGifFile.extractGifImages(sourceFile, file, fromIndex - 1, toIndex - 1);
                        return filenames != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        multipleFilesGenerated(filenames);
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editAction(ActionEvent event) {
        try {
            ImageManufactureController controller
                    = (ImageManufactureController) openStage(CommonValues.ImageManufactureFxml);
            controller.selectSourceFile(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editFrames(ActionEvent event) {
        try {
            ImageGifEditerController controller
                    = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml);
            controller.selectSourceFile(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void showGifImage(int start) {
        try {
            if (images == null || images.length == 0) {
                return;
            }
            setPauseButton(false);
            currentIndex = start;
            setCurrentFrame();
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        showGifImage(currentIndex);
                    });
                }
            }, currentDelay);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void showGifFrame(int frame) {
        try {
            if (images == null || images.length == 0) {
                return;
            }
            if (timer != null) {
                timer.cancel();
            }
            currentIndex = frame;
            setCurrentFrame();
            setPauseButton(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // index is 1-based
    protected void setCurrentFrame() {
        try {
            if (currentIndex > images.length) {
                currentIndex = 1;
            } else if (currentIndex < 1) {
                currentIndex = images.length;
            }
            currentDelay = (int) (delays[currentIndex - 1] * 10 / speed);
            imageView.setImage(images[currentIndex - 1]);
            refinePane();
            promptLabel.setText(AppVariables.message("TotalFrames") + ": " + images.length + "  "
                    + AppVariables.message("CurrentFrame") + ": " + currentIndex + "  "
                    + AppVariables.message("DurationMilliseconds") + ": " + currentDelay + "  "
                    + AppVariables.message("Size") + ": " + (int) images[currentIndex - 1].getWidth()
                    + "*" + (int) images[currentIndex - 1].getHeight());
            isSettingValues = true;
            frameBox.getSelectionModel().select(currentIndex + "");
            isSettingValues = false;

            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                    + " - " + message("Frame") + " " + currentIndex);

            currentIndex++;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public ImageGifViewerController refresh() {
        ImageGifViewerController c = (ImageGifViewerController) refreshBase();
        if (c == null) {
            return null;
        }
        c.loadImage(sourceFile);
        return c;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (sourceFile == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    "gif", CommonFxValues.GifExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        return FileTools.copyFile(sourceFile, file);
                    }

                    @Override
                    protected void whenSucceeded() {
                        popInformation(AppVariables.message("Saved"));
                        ImageGifViewerController controller
                                = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                        controller.loadImage(file.getAbsolutePath());
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
