package screen;

import server.controller.ChatController;
import server.domain.Chat;
import server.domain.Room;
import server.domain.Status;
import server.domain.User;
import server.repository.ChatRepository;
import server.repository.RoomRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ChatRoomScreen {
    private final String roomName;
    private final String nickname;
    private final ChatController chatController;
    private final String userStatus;
    private JTextArea status1ChatArea;
    private JTextArea status2ChatArea;
    private JTextField chatInput;
    private Socket sock;
    private PrintWriter pw;
    private BufferedReader br;
    private RoomRepository roomRepository;
    private ChatRepository chatRepository;
    private Map<String, List<PrintWriter>> userMap;

    public ChatRoomScreen(String roomName, String nickname, Socket sock, PrintWriter pw, BufferedReader br, Map<String, List<PrintWriter>> userMap, String userStatus) {
        this.roomName = roomName;
        this.nickname = nickname;
        this.sock = sock;
        this.pw = pw;
        this.br = br;
        this.roomRepository = new RoomRepository();
        this.chatRepository = new ChatRepository();
        this.chatController = new ChatController(userMap);
        this.userMap = userMap;
        this.userStatus = userStatus;
    }

    public void createChatRoomScreen() {
        JFrame frame = new JFrame("토론 플랫폼 - " + roomName);
        Room room = roomRepository.findRoomByName(roomName);
        String firstStatus = room.getFirstStatus();
        String secondStatus = room.getSecondStatus();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 상단 제목 표시
        JLabel titleLabel = new JLabel("토론 주제: " + roomName, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        frame.add(titleLabel, BorderLayout.NORTH);

        // 좌우 분할 패널 생성
        JPanel chatPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 좌측 (status1) 채팅 영역
        JPanel status1Panel = new JPanel(new BorderLayout());
        JLabel status1Label = new JLabel(firstStatus, SwingConstants.CENTER);
        status1Label.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status1ChatArea = new JTextArea();
        status1ChatArea.setEditable(false);
        status1ChatArea.setLineWrap(true); // 자동 줄바꿈 설정
        status1ChatArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
        JScrollPane status1ScrollPane = new JScrollPane(status1ChatArea);
        status1Panel.add(status1Label, BorderLayout.NORTH);
        status1Panel.add(status1ScrollPane, BorderLayout.CENTER);
        //chatPanel.add(status1ScrollPane);

        // 우측 (status2) 채팅 영역
        JPanel status2Panel = new JPanel(new BorderLayout());
        JLabel status2Label = new JLabel(secondStatus, SwingConstants.CENTER);
        status2Label.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status2ChatArea = new JTextArea();
        status2ChatArea.setEditable(false);
        status2ChatArea.setLineWrap(true); // 자동 줄바꿈 설정
        status2ChatArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
        JScrollPane status2ScrollPane = new JScrollPane(status2ChatArea);
        status2Panel.add(status2Label, BorderLayout.NORTH);
        status2Panel.add(status2ScrollPane, BorderLayout.CENTER);
        //chatPanel.add(status2ScrollPane);

        chatPanel.add(status1Panel);
        chatPanel.add(status2Panel);
        frame.add(chatPanel, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendButton = new JButton("전송");

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);


        SwingUtilities.invokeLater(() -> {
            loadChatHistory();
        });

        System.out.println("ChatHistory 호출 확인");

        // 서버에서 메시지 수신 처리
        new Thread(this::receiveMessages).start();

        // 전송 버튼 액션 리스너
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = chatInput.getText(); // 사용자 입력
                    if (message.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "메시지를 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    pw.println("/chat " + message + " " + userStatus); // 서버로 전송
                    pw.flush();
                    chatInput.setText(""); // 입력 필드 초기화
                    Room room = roomRepository.findRoomByName(roomName);
                    String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
                    String formattedMessage = "[" + timestamp + "] " + nickname + " : " + message;
                    // 메시지를 상태에 따라 출력
                    if (userStatus.equals(room.getFirstStatus())) {
                        status1ChatArea.append(formattedMessage + "\n");
                        int lineCount = calculateLineCount(formattedMessage, status1ChatArea);
                        syncLineCounts(status2ChatArea, lineCount);
                    } else if (userStatus.equals(room.getSecondStatus())) {
                        status2ChatArea.append(formattedMessage + "\n");
                        int lineCount = calculateLineCount(formattedMessage, status2ChatArea);
                        syncLineCounts(status1ChatArea, lineCount);
                    } else {
                        System.out.println("User status does not match any room status.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "메시지 전송 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        frame.setVisible(true);
    }


    private void receiveMessages() {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                Room room = roomRepository.findRoomByName(roomName);
                if (room == null) {
                    System.out.println("Room not found: " + roomName);
                    continue;
                }
                // 메시지 처리
                if (line.startsWith(room.getFirstStatus() + ":")) {
                    status1ChatArea.append(line.replace(room.getFirstStatus() + ":", "") + "\n");
                } else if (line.startsWith(room.getSecondStatus() + ":")) {
                    status2ChatArea.append(line.replace(room.getSecondStatus() + ":", "") + "\n");
                } else {
                    System.out.println("Unexpected message format: " + line);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error reading message: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "메시지 수신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "메시지 수신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChatHistory() {
        try {
            Room room = roomRepository.findRoomByName(roomName);
            List<Chat> chatHistory = chatRepository.findChatHistory(room);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            for (Chat chat : chatHistory) {
                String formattedTimestamp = timeFormat.format(chat.getTimestamp()); // 타임스탬프 변환
                String formattedMessage = "[" + formattedTimestamp + "] " + chat.getUserName() + " : " + chat.getMessage();
                if (chat.getStatus().equals(room.getFirstStatus())) {
                    status1ChatArea.append(formattedMessage + "\n");
                    int lineCount = calculateLineCount(formattedMessage, status1ChatArea);
                    syncLineCounts(status2ChatArea, lineCount);
                } else if (chat.getStatus().equals(room.getSecondStatus())) {
                    status2ChatArea.append(formattedMessage + "\n");
                    int lineCount = calculateLineCount(formattedMessage, status2ChatArea);
                    syncLineCounts(status1ChatArea, lineCount);
                } else {
                    // 상태가 없는 메시지 처리
                    System.out.println("Unrecognized Status: " + chat.getMessage());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int calculateLineCount(String message, JTextArea textArea) {
        int areaWidth = textArea.getWidth();
        if (areaWidth <= 0) {
            areaWidth = 800; // 기본 너비 설정 (UI 초기화 전에도 작동 가능하도록)
        }
        FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
        int textWidth = fontMetrics.stringWidth(message);
        // 가로 너비 기준으로 몇 줄이 필요한지 계산
        int lines = (int) Math.ceil((double) textWidth / areaWidth);
        return Math.max(lines, 1); // 최소 1줄
    }

    private void syncLineCounts(JTextArea textAreaToSync, int calculatedLines) {
        for (int i = 0; i < calculatedLines; i++) {
            textAreaToSync.append("\n");
        }
    }

}
