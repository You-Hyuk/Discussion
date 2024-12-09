package client.screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class StartScreen {
    private String nickname;
    private JTextField nicknameField;
    BufferedReader br = null;
    PrintWriter pw = null;

    public static void main(String[] args) {
        // 인스턴스 생성 후 createStartScreen 호출
        StartScreen screen = new StartScreen();
        screen.createStartScreen();
    }

    public void createStartScreen() {

        JFrame frame = new JFrame("토론 플랫폼 - 시작 화면");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(null); // Absolute layout for precise placement
        frame.getContentPane().setBackground(Color.WHITE); // 흰색 배경
        frame.setLocationRelativeTo(null);

        // 상단 타이틀
        JLabel titleLabel = new JLabel("토론 플랫폼");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 40));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(200, 50, 400, 50); // Absolute position
        frame.add(titleLabel);

        // 닉네임 설정 패널
        JPanel nicknamePanel = new JPanel();
        nicknamePanel.setBackground(new Color(220, 220, 220)); // 연한 회색 배경
        nicknamePanel.setBounds(200, 150, 400, 200); // Absolute position
        nicknamePanel.setLayout(null); // Nested absolute layout

        JLabel nicknameLabel = new JLabel("닉네임 설정");
        nicknameLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        nicknameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nicknameLabel.setBounds(100, 20, 200, 30);
        nicknamePanel.add(nicknameLabel);

        nicknameField = new JTextField("닉네임을 입력하세요.");
        nicknameField.setHorizontalAlignment(JTextField.CENTER);
        nicknameField.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        nicknameField.setForeground(Color.GRAY); // 기본 텍스트 색상
        nicknameField.setBounds(100, 70, 200, 40); // Absolute position
        nicknameField.setFocusable(false);

        // MouseListener 추가
        nicknameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!nicknameField.isFocusable()) {
                    nicknameField.setFocusable(true); // 클릭 시 포커스 가능하도록 변경
                    nicknameField.requestFocusInWindow(); // 포커스 요청
                }
                if (nicknameField.getText().equals("닉네임을 입력하세요.")) {
                    nicknameField.setText(""); // 플레이스홀더 제거
                    nicknameField.setForeground(Color.BLACK); // 텍스트 색상 변경
                }
            }
        });

        // FocusListener 추가
        nicknameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                if (nicknameField.getText().isEmpty()) {
                    nicknameField.setText("닉네임을 입력하세요.");
                    nicknameField.setForeground(Color.GRAY);
                }
            }
        });

        nicknamePanel.add(nicknameField);

        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        confirmButton.setBounds(150, 130, 100, 40);
        confirmButton.setFocusPainted(false);
        confirmButton.addActionListener(e -> {
            nickname = nicknameField.getText().trim();
            if (nickname.isEmpty() || nickname.equals("닉네임을 입력하세요.")) {
                JOptionPane.showMessageDialog(frame, "닉네임을 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // 서버 연결
                // StartScreen.java에서 서버 연결 초기화
                Socket sock = new Socket("192.168.93.254", 10001); //192.168.67.228
                pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                // 서버로 닉네임 전송
                pw.println(nickname);
                pw.flush();

                // MainScreen에 연결된 소켓 전달
                frame.dispose();
                MainScreen mainScreen = new MainScreen(nickname, sock, pw, br);
                mainScreen.createMainScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "서버 연결에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                System.out.println(ex);
            }
        });
        nicknamePanel.add(confirmButton);

        frame.add(nicknamePanel);
        frame.setVisible(true);
    }
}