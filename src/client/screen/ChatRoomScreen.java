package client.screen;

import client.handler.ChatHandler;
import client.handler.RoomHandler;
import server.domain.Room;

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

import static server.dto.SuccessResponse.*;

public class ChatRoomScreen {
    private final String roomName;
    private final String nickname;
    private final String userStatus;
    private JPanel status1ChatArea;
    private JPanel status2ChatArea;
    private JTextField chatInput;
    private Socket sock;
    private PrintWriter pw;
    private BufferedReader br;
    private RoomHandler roomHandler;
    private ChatHandler chatHandler;


    public ChatRoomScreen(String roomName, String nickname, Socket sock, PrintWriter pw, BufferedReader br, String userStatus) {
        this.roomName = roomName;
        this.nickname = nickname;
        this.sock = sock;
        this.pw = pw;
        this.br = br;
        this.userStatus = userStatus;

        this.roomHandler = new RoomHandler(pw);
        this.chatHandler = new ChatHandler(pw);
    }

    public void createChatRoomScreen() {
        JFrame frame = new JFrame("토론 플랫폼 - " + roomName);
        String[] roomData = null;
        String response;

        try {
            roomHandler.findRoom(this.roomName);
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals(FIND_ROOM_SUCCESS.name()))
                    break;
                roomData = response.split(",");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        //roomname,firststatus,secondstatus,username
        String roomName = roomData[0];
        String firstStatus = roomData[4];
        String secondStatus = roomData[5];
        String userName = roomData[1];
        Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);

        String likeMost1 = "status1 최다 좋아요 메시지";
        String likeMost2 = "status2 최다 좋아요 메시지";
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
        exitButton.addActionListener(e -> {
            if(userStatus.equals("중립")) {
                exitPopup(frame, roomName);
            }
            else {
                frame.dispose();
                MainScreen mainScreen = new MainScreen(nickname, sock, pw, br);
                mainScreen.createMainScreen();
                roomHandler.exitRoom(roomName);
            }
        });

        topPanel.add(exitButton, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // 좌우 분할 패널 생성
        JPanel chatPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 좌측 (status1) 채팅 영역
        JPanel status1Panel = new JPanel(new BorderLayout());
        JPanel status1Header = new JPanel(new GridLayout(2, 1));
        JLabel status1Label = new JLabel(firstStatus, SwingConstants.CENTER);
        JLabel status1Like = new JLabel(likeMost1, SwingConstants.CENTER);
        status1Header.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status1Header.add(status1Label);
        status1Header.add(status1Like);

        status1ChatArea = new JPanel();
        status1ChatArea.setLayout(new BoxLayout(status1ChatArea, BoxLayout.Y_AXIS)); // 세로로 정렬
        JScrollPane status1ScrollPane = new JScrollPane(status1ChatArea);

        status1Panel.add(status1Header, BorderLayout.NORTH); // 헤더 패널 추가
        status1Panel.add(status1ScrollPane, BorderLayout.CENTER); // 스크롤 패널 추가

        // 우측 (status2) 채팅 영역
        JPanel status2Panel = new JPanel(new BorderLayout());
        JPanel status2Header = new JPanel(new GridLayout(2, 1));
        JLabel status2Label = new JLabel(secondStatus, SwingConstants.CENTER);
        JLabel status2Like = new JLabel(likeMost2, SwingConstants.CENTER);
        status2Header.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status2Header.add(status2Label);
        status2Header.add(status2Like);

        status2ChatArea = new JPanel();
        status2ChatArea.setLayout(new BoxLayout(status2ChatArea, BoxLayout.Y_AXIS)); // 세로로 정렬
        JScrollPane status2ScrollPane = new JScrollPane(status2ChatArea);

        status2Panel.add(status2Header, BorderLayout.NORTH); // 헤더 패널 추가
        status2Panel.add(status2ScrollPane, BorderLayout.CENTER); // 스크롤 패널 추가

        chatPanel.add(status1Panel);
        chatPanel.add(status2Panel);
        frame.add(chatPanel, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendButton = new JButton("전송");

        if (userStatus.equals("중립")) {
            chatInput.setEnabled(false);
            chatInput.setText("중립 상태에서는 채팅을 보낼 수 없습니다.");
            sendButton.setEnabled(false);
        }

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);


        SwingUtilities.invokeLater(() -> {
            loadChatHistory();
        });

        // 전송 버튼 액션 리스너
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String response = null;
                try {
                    String chat = chatInput.getText(); // 사용자 입력
                    if (chat.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "메시지를 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    chatHandler.sendChat(roomName, chat, userStatus);

                    while ((response = br.readLine()) != null) {
                        if (response.equals(SEND_CHAT_SUCCESS.name()))
                            break;
                    }
                    chatInput.setText(""); // 입력 필드 초기화

                    String[] roomData = null;
                    try {
                        roomHandler.getRoomList();
                        // 서버 응답 처리
                        while ((response = br.readLine()) != null) {
                            if (response.equals(GET_ROOM_LIST_SUCCESS.name()))
                                break;
                            roomData = response.split(",");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    //roomname,firststatus,secondstatus,username
                    Room room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);
                    String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
                    String formattedMessage = "[" + timestamp + "] " + nickname + " : " + chat;
                    // 메시지를 상태에 따라 출력
                    addMessage(formattedMessage, userStatus, room);
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
            roomHandler.getRoomList();

            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals(GET_ROOM_LIST_SUCCESS.name())) break;
                roomData = response.split(",");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // 유효한 데이터가 없을 경우
        if (roomData == null || roomData.length < 6) {
            JOptionPane.showMessageDialog(null, "방 정보가 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        room = new Room(roomData[0], roomData[4], roomData[5], roomData[1]);

        try {
            chatHandler.getChaHistory(roomName);

            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm"); // 입력 포맷
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // 출력 포맷 (시간)
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals(GET_CHAT_HISTORY_SUCCESS.name()))
                    break;
                // 쉼표로 채팅 기록 분리
                String[] chatEntries = response.split("\t");
                for (String chatEntry : chatEntries) {
                    String[] chatData = chatEntry.split(" ");
                    timestamp = chatData[0];
                    userName = chatData[1];
                    message = chatData[2];
                    status = chatData[3];
                    likeCount = chatData[4];
                    String formattedTimestamp = "";
                    try {
                        Date date = inputFormat.parse(timestamp);
                        formattedTimestamp = timeFormat.format(date);
                    } catch (Exception ex) {
                        formattedTimestamp = "알 수 없음";
                    }
                    // 포맷된 메시지 생성
                    String formattedMessage = "[" + formattedTimestamp + "]" + userName + " : " + message;
                    // 메시지 상태에 따라 화면에 표시
                    addMessage(formattedMessage, status, room);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addMessage(String message, String status, Room room) {
        JLabel messageLabel = new JLabel(message);
        messageLabel.setOpaque(true);
        messageLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 마우스 리스너 추가
        messageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(null, "클릭된 메시지: " + message, "메시지 클릭", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        int linesToSync;
        if (status.equals(room.getFirstStatus())) {
            status1ChatArea.add(messageLabel);
            linesToSync = calculateLineCount(messageLabel); // 새 메시지가 차지하는 줄 수 계산
            syncLineCounts(status2ChatArea, linesToSync);
        } else if (status.equals(room.getSecondStatus())) {
            status2ChatArea.add(messageLabel);
            linesToSync = calculateLineCount(messageLabel); // 새 메시지가 차지하는 줄 수 계산
            syncLineCounts(status1ChatArea, linesToSync);
        }

        // UI 갱신
        status1ChatArea.revalidate();
        status1ChatArea.repaint();
        status2ChatArea.revalidate();
        status2ChatArea.repaint();
    }

    private int calculateLineCount(JLabel label) {
        FontMetrics fontMetrics = label.getFontMetrics(label.getFont());
        int textWidth = fontMetrics.stringWidth(label.getText()); // 텍스트의 가로 길이
        int panelWidth = status1ChatArea.getWidth(); // 패널의 현재 너비

        // 패널의 너비에 맞춰 메시지가 몇 줄로 나뉘는지 계산
        int lines = (int) Math.ceil((double) textWidth / panelWidth);
        return Math.max(lines, 1); // 최소 1줄
    }

    private void syncLineCounts(JPanel targetPanel, int linesToAdd) {
        for (int i = 0; i < linesToAdd; i++) {
            JLabel emptyLine = new JLabel(" ");
            emptyLine.setOpaque(true);
            emptyLine.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
            emptyLine.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            targetPanel.add(emptyLine);
        }
    }

    private void exitPopup(JFrame parentFrame, String roomName) {
        // 팝업 다이얼로그 생성
        JDialog exitDialog = new JDialog(parentFrame, "토론방 퇴장", true);
        exitDialog.setSize(350, 300);
        exitDialog.setLayout(null);
        JLabel exitTitle = new JLabel("토론방 퇴장", SwingConstants.CENTER);
        exitTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        exitTitle.setBounds(0, 10, 350, 30);
        exitTitle.setForeground(Color.BLACK); // 검정색 텍스트
        exitDialog.add(exitTitle);

        String[] roomData = null;
        String response;
        try {
            roomHandler.findRoom(roomName);
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                if (response.equals(FIND_ROOM_SUCCESS.name()))
                    break;
                roomData = response.split(",");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        String firstStatus = roomData[4];
        String secondStatus = roomData[5];


        // STATUS 버튼 패널
        JPanel statusPanel = new JPanel();
        statusPanel.setBounds(70, 80, 200, 40);
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
        confirmButton.setBounds(125, 150, 100, 40);
        confirmButton.setBackground(Color.WHITE);
        confirmButton.setForeground(Color.BLACK);
        confirmButton.addActionListener(event -> {
            try {
                roomHandler.voteDiscussion(roomName, selectedStatus[0]);
                roomHandler.exitRoom(roomName);

                // 팝업 및 현재 창 닫기
                exitDialog.dispose();
                parentFrame.dispose();

                // MainScreen 생성 및 테이블 갱신
                MainScreen mainScreen = new MainScreen(nickname, sock, pw, br);
                mainScreen.createMainScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(exitDialog, "퇴장 처리 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitDialog.add(confirmButton);
        exitDialog.setLocationRelativeTo(parentFrame); // 창 중앙에 표시
        exitDialog.setVisible(true);
    }

}