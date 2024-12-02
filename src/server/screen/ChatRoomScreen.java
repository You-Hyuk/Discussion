package server.screen;

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
        String[] roomData = null;
        try {
            String response;
            // 서버에 방 리스트 요청
            pw.println("/list");
            pw.flush();
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals("LIST_END")) break;
                roomData = response.split(",");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
        //roomame,firststatus,secondstatus,username
        Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);
        String firstStatus = room.getFirstStatus();
        String secondStatus = room.getSecondStatus();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("토론 주제: " + roomName, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
        JButton exitButton = new JButton("\uD83D\uDEAA");
        exitButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 30));
        exitButton.setFocusPainted(false); // 포커스 사각형 비활성화
        exitButton.setPreferredSize(new Dimension(50, 50)); // 버튼 크기 설정
        exitButton.setBackground(new Color(240, 128, 128)); // 연한 빨간색
        exitButton.setForeground(Color.WHITE); // 버튼 텍스트 색상
        exitButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 여백
        exitButton.addActionListener(e -> exitPopup(frame, room));

        topPanel.add(exitButton, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

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

        // 서버에서 메시지 수신 처리
        //new Thread(this::receiveMessages).start();

        // 전송 버튼 액션 리스너
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String response = null;
                try {
                    String message = chatInput.getText(); // 사용자 입력
                    if (message.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "메시지를 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    pw.println("/chat " + message + " " + userStatus); // 서버로 전송
                    pw.flush();

//                    // 새로운 /broadcast 명령 전송
//                    pw.println("/broadcast " + roomName + " " + message + " " + nickname);
//                    System.out.println("브로드캐스트 명령 전송: " + roomName + ", " + message + ", " + nickname);
//                    pw.flush();

                    while ((response = br.readLine()) != null) {
                        if (response.equals("CHAT_END")) break;
                    }
                    chatInput.setText(""); // 입력 필드 초기화

                    String[] roomData = null;
                    try {
                        // 서버에 방 리스트 요청
                        pw.println("/list");
                        pw.flush();
                        // 서버 응답 처리
                        while ((response = br.readLine()) != null) {
                            if (response.equals("LIST_END")) break;
                            roomData = response.split(",");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    //roomame,firststatus,secondstatus,username
                    Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);
                    System.out.println("전송에서 room 확인: " + room);
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
                String[] roomData = null;
                try {
                    String response;
                    pw.println("/list");
                    pw.flush();
                    // 서버 응답 처리
                    while ((response = br.readLine()) != null) {
                        System.out.println("receiveMessages에서 클라이언트 받은 데이터 = " + response);
                        if (response.equals("LIST_END")) break;
                        //roomname,username,firststatuscount,secondstatuscount,firststatus,secondstatus
                        roomData = response.split(",");
                    }
                } catch (Exception ex) {
                    System.out.println("loadChatHistory 오류: " + ex);
                }

                // roomData가 null인지 확인
                if (roomData == null || roomData.length < 6) {
                    System.out.println("receiveMessage에서 roomData 확인: " + roomData);
                    System.out.println("유효한 roomData를 찾을 수 없습니다.");
                    continue; // 다음 반복으로 이동
                }

                Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);

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
        String userName;
        String timestamp;
        String message;
        String status;
        String likeCount;
        String[] roomData = null;
        String response;
        Room room = null;
        try {
            // 서버에 방 리스트 요청
            pw.println("/list");
            pw.flush();
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals("LIST_END")) break;
                roomData = response.split(",");
            }
            //Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);
        try {
            pw.println("/history " + room.getRoomName()); // 방 이름 예: "토론1"
            pw.flush();

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // 입력 포맷
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // 출력 포맷 (시간)

            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                // 맨 앞의 "[" 제거
                if (response.startsWith("[")) {
                    response = response.replaceFirst("\\[", " ");
                }
                if (response.equals("HISTORY_END")) break;

                if (response.contains("해당 방의 채팅 기록이 없습니다.")) {
                    System.out.println("채팅 기록 없음: " + response);
                    break; // 기록 없음 메시지는 무시하고 다음으로 진행
                }

                // 쉼표로 채팅 기록 분리
                String[] chatEntries = response.split(",");
                for (String chatEntry : chatEntries) {
                    // 공백으로 세부 데이터 분리
                    String[] chatData = chatEntry.split("\t");
//                    if (chatData.length < 5) {
//                        System.out.println("Invalid chat data: " + chatEntry);
//                        continue; // 유효하지 않은 데이터 무시
//                    }

                    userName = chatData[0];
                    timestamp = chatData[3];
                    message = chatData[1];
                    status = chatData[2];
                    likeCount = chatData[4];

                    String formattedTimestamp = "";
                    try {
                        // 타임스탬프 문자열을 Date로 변환 후 포맷
                        Date date = inputFormat.parse(timestamp);
                        formattedTimestamp = timeFormat.format(date);
                    } catch (Exception ex) {
                        System.out.println("타임스탬프 변환 중 오류: " + ex.getMessage());
                        formattedTimestamp = "알 수 없음"; // 오류 발생 시 기본값
                    }
                    // 포맷된 메시지 생성
                    String formattedMessage = "[" + formattedTimestamp + "]" + userName + " : " + message;

                    // 메시지 상태에 따라 화면에 표시
                    if (status1ChatArea != null && status.equals(room.getFirstStatus())) {
                        status1ChatArea.append(formattedMessage + "\n");
                        int lineCount = calculateLineCount(formattedMessage, status1ChatArea);
                        syncLineCounts(status2ChatArea, lineCount);
                    } else if (status2ChatArea != null && status.equals(room.getSecondStatus())) {
                        status2ChatArea.append(formattedMessage + "\n");
                        int lineCount = calculateLineCount(formattedMessage, status2ChatArea);
                        syncLineCounts(status1ChatArea, lineCount);
                    } else {
                        // 상태가 없는 메시지 처리 (중립 상태)
                        System.out.println("Unrecognized Status: " + message);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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

    private void exitPopup(JFrame parentFrame, Room room) {
        // 팝업 다이얼로그 생성
        JDialog exitDialog = new JDialog(parentFrame, "토론방 퇴장", true);
        exitDialog.setSize(350, 250);
        exitDialog.setLayout(null);
        JLabel exitTitle = new JLabel("토론방 퇴장", SwingConstants.CENTER);
        exitTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        exitTitle.setBounds(0, 10, 300, 30);
        exitTitle.setForeground(Color.BLACK); // 검정색 텍스트
        exitDialog.add(exitTitle);

        String firstStatus = room.getFirstStatus();
        String secondStatus = room.getSecondStatus();

        // STATUS 버튼 패널
        JPanel statusPanel = new JPanel();
        statusPanel.setBounds(50, 60, 200, 40);
        statusPanel.setLayout(new GridLayout(1, 2, 10, 0)); // 상태 버튼 간격

        JButton status1Button = new JButton(firstStatus);
        JButton status2Button = new JButton(secondStatus);

        // 기본 스타일 설정
        status1Button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        status2Button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        status1Button.setBackground(Color.WHITE);
        status2Button.setBackground(Color.WHITE);

        // 상태 선택 추적 변수
        final String[] selectedStatus = {null};

        // 버튼 클릭 이벤트 (선택 상태 표시)
        status1Button.addActionListener(event -> {
            selectedStatus[0] = firstStatus;
            status1Button.setBackground(new Color(173, 216, 230)); // 연한 파란색
            status2Button.setBackground(Color.WHITE);
        });

        status2Button.addActionListener(event -> {
            selectedStatus[0] = secondStatus;
            status1Button.setBackground(Color.WHITE);
            status2Button.setBackground(new Color(173, 216, 230)); // 연한 파란색
        });

        status1Button.setFocusPainted(false);
        status2Button.setFocusPainted(false);

        statusPanel.add(status1Button);
        statusPanel.add(status2Button);
        exitDialog.add(statusPanel);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(100, 120, 100, 30);
        confirmButton.setBackground(Color.WHITE);
        confirmButton.setForeground(Color.BLACK);
        confirmButton.addActionListener(event -> {
            if (selectedStatus[0] == null) {
                JOptionPane.showMessageDialog(exitDialog, "STATUS를 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
                // 퇴장 로직 (나가기 처리)
                System.out.println("퇴장 상태: " + selectedStatus[0]);
                exitDialog.dispose();
                parentFrame.dispose(); // 채팅방 창 닫기
                // 메인 화면 생성
                MainScreen mainScreen = new MainScreen(nickname, sock, pw, br);
                mainScreen.createMainScreen();
            }
        });

        exitDialog.add(confirmButton);
        exitDialog.setLocationRelativeTo(parentFrame); // 창 중앙에 표시
        exitDialog.setVisible(true);
    }

}
