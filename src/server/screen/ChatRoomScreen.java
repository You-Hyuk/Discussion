package server.screen;

import server.controller.ChatController;
import server.domain.Chat;
import server.domain.Room;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        status1ChatArea = new JTextArea();
        status1ChatArea.setEditable(false);
        JScrollPane status1ScrollPane = new JScrollPane(status1ChatArea);
        chatPanel.add(status1ScrollPane);

        // 우측 (status2) 채팅 영역
        status2ChatArea = new JTextArea();
        status2ChatArea.setEditable(false);
        JScrollPane status2ScrollPane = new JScrollPane(status2ChatArea);
        chatPanel.add(status2ScrollPane);

        frame.add(chatPanel, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendButton = new JButton("전송");

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // 채팅 기록 로드
        loadChatHistory();

        // 서버에서 메시지 수신 처리
        new Thread(this::receiveMessages).start();

        // 전송 버튼 액션 리스너
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatInput.getText().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    chatInput.setText(""); // 입력 필드 초기화
                }
            }
        });

        frame.setVisible(true);
    }

    private void sendMessage(String message) {
        try {
            Room room = roomRepository.findRoomByName(roomName);
            if (room == null) {
                throw new IllegalStateException("Room not found: " + roomName);
            }
            // 초기화 확인
            if (pw == null) {
                System.out.println("PrintWriter is null. Check server connection.");
                return;
            }
            if (userStatus == null) {
                System.out.println("User status is null. Check the input status.");
                return;
            }
            if (userMap == null) {
                System.out.println("userMap is null. Check the input status.");
                return;
            }
            // 현재 시간 추가
            String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
            // 메시지 구성: [시간] 유저 이름 : 내용
            String formattedMessage = userStatus + "[" + timestamp + "] " + nickname + " : " + message;

            pw.println(formattedMessage);
            pw.flush();

            // 메시지를 상태에 따라 출력
            if (userStatus.equals(room.getFirstStatus())) {
                status1ChatArea.append("[" + timestamp + "] " + nickname + " : " + message + "\n");
            } else if (userStatus.equals(room.getSecondStatus())) {
                status2ChatArea.append("[" + timestamp + "] " + nickname + " : " + message + "\n");
            } else {
                System.out.println("User status does not match any room status.");
            }

            chatController.chat(room, new User(nickname, pw), new HashMap<>(userMap), userStatus, message);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "메시지 전송 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
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
            for (Chat chat : chatHistory) {
                if (chat.getStatus().equals(room.getFirstStatus())) {
                    status1ChatArea.append(chat.getMessage() + "\n");
                } else if (chat.getStatus().equals(room.getSecondStatus())) {
                    status2ChatArea.append(chat.getMessage() + "\n");
                } else {
                    // 상태가 없는 메시지 처리
                    System.out.println("Unrecognized Status: " + chat.getMessage());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
