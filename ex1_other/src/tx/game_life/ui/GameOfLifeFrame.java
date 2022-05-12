package tx.game_life.ui;

import javax.swing.*;

import tx.game_life.model.CellMatrix;
import tx.game_life.util.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class GameOfLifeFrame extends JFrame {

    private JButton openFileBtn = new JButton("从文件载入配置");
    private JButton startGameBtn = new JButton("持续演化");
    private JLabel durationPromtLabel = new JLabel("动画间隔设置(ms为单位)");
    private JButton stepGameBtn = new JButton("演化一步");
    private JButton saveGameBtn = new JButton("保存状态");
    private JTextField durationTextField = new JTextField();
    /**
     * 游戏是否开始的标志
     */
    private boolean isStart = false;

    /**
     * 游戏结束的标志
     */
    private boolean stop = false;

    private CellMatrix cellMatrix = null;
    private JPanel buttonPanel = new JPanel(new GridLayout(3, 2));
    private JPanel gridPanel = new JPanel();

    private JTextField[][] textMatrix;


    /**
     * 动画默认间隔200ms
     */
    private static final int DEFAULT_DURATION = 200;

    //动画间隔
    private int duration = DEFAULT_DURATION;

    public GameOfLifeFrame() {
        setTitle("生命游戏");
        openFileBtn.addActionListener(new OpenFileActioner());
        startGameBtn.addActionListener(new StartGameActioner());
        stepGameBtn.addActionListener(new stepGameActioner());
        saveGameBtn.addActionListener(new saveGameBtnActioner());
        buttonPanel.add(openFileBtn);
        buttonPanel.add(startGameBtn);
        buttonPanel.add(durationPromtLabel);
        buttonPanel.add(durationTextField);
        buttonPanel.add(stepGameBtn);
        buttonPanel.add(saveGameBtn);
        buttonPanel.setBackground(Color.WHITE);

        getContentPane().add("North", buttonPanel);

        this.setSize(1000, 1200);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        saveGameBtn.setEnabled(false);
    }


    private class OpenFileActioner implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fcDlg = new JFileChooser(".");
            fcDlg.setDialogTitle("请选择初始配置文件");
            int returnVal = fcDlg.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                isStart = false;
                stop = true;
                startGameBtn.setText("持续演化");

                String filepath = fcDlg.getSelectedFile().getPath();
                cellMatrix = Utils.initMatrixFromFile(filepath);
                initGridLayout();
                showMatrix();
                gridPanel.updateUI();
                saveGameBtn.setEnabled(true);
            }
        }


    }

    private void showMatrix() {
        int[][] matrix = cellMatrix.getMatrix();
        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[0].length; x++) {
                if (matrix[y][x] == 1) {
                    textMatrix[y][x].setBackground(Color.BLACK);
                } else {
                    textMatrix[y][x].setBackground(Color.WHITE);
                }
            }
        }
    }

    /**
     * 创建显示的gridlayout布局
     */
    private void initGridLayout() {
        int rows = cellMatrix.getHeight();
        int cols = cellMatrix.getWidth();
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(rows, cols));
        textMatrix = new JTextField[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                JTextField text = new JTextField();
                textMatrix[y][x] = text;
                gridPanel.add(text);
            }
        }
        add("Center", gridPanel);
    }

    private class saveGameBtnActioner implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fcDlg = new JFileChooser(".");
            fcDlg.setDialogTitle("请选择保存文件");
            int returnVal = fcDlg.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String filepath = fcDlg.getSelectedFile().getPath();
                Utils.saveMatrixToFile(cellMatrix,filepath);
            }
        }

    }  

    private class StartGameActioner implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            if(cellMatrix == null)
                return;
                
            if (!isStart){
                //获取时间
                try {
                    duration = Integer.parseInt(durationTextField.getText().trim());
                } catch (NumberFormatException e1) {
                    duration = DEFAULT_DURATION;
                }

                new Thread(new GameControlTask()).start();
                isStart = true;
                stop = false;
                startGameBtn.setText("暂停演化");
                saveGameBtn.setEnabled(false);
            } else {
                stop = true;
                isStart = false;
                startGameBtn.setText("持续演化");
                saveGameBtn.setEnabled(true);
            }
        }
    }

    private class stepGameActioner implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            if (cellMatrix != null) 
                try{
                    cellMatrix.transform();
                    showMatrix();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
        }
    }

    private class GameControlTask implements Runnable {

        @Override
        public void run() {
            while (!stop) {
                cellMatrix.transform();
                showMatrix();

                try {
                    TimeUnit.MILLISECONDS.sleep(duration);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}
