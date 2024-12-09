package client.screen;

import client.handler.ChatHandler;
import client.handler.RoomHandler;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static server.dto.SuccessResponseCommand.*;

public class ChatRoomScreen {
    private final String roomName;
    private final String userName;
    private final String status;
    private JPanel status1ChatArea;
    private JPanel status2ChatArea;
    private JTextField chatInput;
    private Socket sock;
    private PrintWriter pw;
    private BufferedReader br;
    private RoomHandler roomHandler;
    private ChatHandler chatHandler;

    public ChatRoomScreen(String roomName, String userName, Socket sock, PrintWriter pw, BufferedReader br, String status) {
        this.roomName = roomName;
        this.userName = userName;
        this.sock = sock;
        this.pw = pw;
        this.br = br;
        this.status = status;
        this.roomHandler = new RoomHandler(pw, userName);
        this.chatHandler = new ChatHandler(pw, userName);
    }

    public void createChatRoomScreen() {

        // 툴팁의 폰트 크기 변경
        UIManager.put("ToolTip.font", new Font("Malgun Gothic", Font.PLAIN, 20)); // 원하는 크기와 폰트 설정
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Color.GRAY)); // 테두리 설정

        JFrame frame = new JFrame("토론 플랫폼 - " + roomName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        String[] roomData = null;
        String response;
        HashMap<String, String> parseResponse = new HashMap<>();
        String command = " ";
        String body = " ";

        try {
            roomHandler.findRoom(roomName);
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                System.out.println(response);
                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");

                if (command.equals(SEND_ROOM_DATA_SUCCESS.name()))
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        String firstStatus = parseBody(body).get("FirstStatus");
        String secondStatus = parseBody(body).get("SecondStatus");


        String likeMost1 = "status1 최다 좋아요 메시지";
        String likeMost2 = "status2 최다 좋아요 메시지";
//        try{
//            chatHandler.getChaHistory(roomName);
//
//            String mostLikedMessage1="";
//            Integer maxLikes1=-1;
//            String mostLikedMessage2="";
//            Integer maxLikes2=-1;
//            while ((response = br.readLine()) != null) {
//                if (response.equals(GET_CHAT_HISTORY_SUCCESS.name()))
//                    break;
//                String[] chatEntries = response.split("\n");
//                for (String chatEntry : chatEntries) {
//                    String[] chatData = chatEntry.split(" ");
//
//                    // 데이터 배열 크기 검증
//                        String message = chatData[2];
//                        String status = chatData[3];
//                        Integer likeCount = Integer.parseInt(chatData[4]);
//
//                        // 최다 좋아요 메시지 찾기
//                        if (status.equals(firstStatus)) {
//                            if (likeCount > maxLikes1) {
//                                maxLikes1 = likeCount;
//                                mostLikedMessage1 = message;
//                            }
//                        } else if (status.equals(secondStatus)) {
//                            if (likeCount > maxLikes2) {
//                                maxLikes2 = likeCount;
//                                mostLikedMessage2 = message;
//                            }
//                        }
//
//                }
//            }
//
//            // 최종 업데이트
//            likeMost1 = mostLikedMessage1;
//            likeMost2 = mostLikedMessage2;
//
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
//            ex.printStackTrace();
//        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("토론 주제: " + roomName, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        titleLabel.setToolTipText("토론 주제: " + roomName);
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
            if(status.equals("중립")) {
                exitPopup(frame, roomName);
            }
            else {
                frame.dispose();
                MainScreen mainScreen = new MainScreen(userName, sock, pw, br);
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
        status1Like.setToolTipText("<html><p style='width:300px;'>" + likeMost1 + "</p></html>");
        status1Label.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status1Like.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status1Header.add(status1Label);
        status1Header.add(status1Like);

        status1ChatArea = new JPanel();
        status1ChatArea.setLayout(new BoxLayout(status1ChatArea, BoxLayout.Y_AXIS)); // 세로로 정렬
        status1ChatArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        status1ChatArea.setBackground(Color.WHITE);
        JScrollPane status1ScrollPane = new JScrollPane(status1ChatArea);
        status1ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        status1ScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        status1Panel.add(status1Header, BorderLayout.NORTH); // 헤더 패널 추가
        status1Panel.add(status1ScrollPane, BorderLayout.CENTER); // 스크롤 패널 추가
        status1Panel.setToolTipText(firstStatus);



        // 우측 (status2) 채팅 영역
        JPanel status2Panel = new JPanel(new BorderLayout());
        JPanel status2Header = new JPanel(new GridLayout(2, 1));
        JLabel status2Label = new JLabel(secondStatus, SwingConstants.CENTER);
        JLabel status2Like = new JLabel(likeMost2, SwingConstants.CENTER);
        status2Like.setToolTipText("<html><p style='width:300px;'>" + likeMost2 + "</p></html>");
        status2Label.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status2Like.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        status2Header.add(status2Label);
        status2Header.add(status2Like);

        status2ChatArea = new JPanel();
        status2ChatArea.setLayout(new BoxLayout(status2ChatArea, BoxLayout.Y_AXIS)); // 세로로 정렬
        status2ChatArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        status2ChatArea.setBackground(Color.WHITE);
        JScrollPane status2ScrollPane = new JScrollPane(status2ChatArea);
        status2ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        status2ScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        status2Panel.add(status2Header, BorderLayout.NORTH); // 헤더 패널 추가
        status2Panel.add(status2ScrollPane, BorderLayout.CENTER); // 스크롤 패널 추가
        status2Panel.setToolTipText(secondStatus);

        chatPanel.add(status1Panel);
        chatPanel.add(status2Panel);
        frame.add(chatPanel, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE); // 배경색 설정
        chatInput = new JTextField();
        JButton sendButton = new JButton("전송");

        if (status.equals("중립")) {
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

        startMessageListener();

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
                    chatHandler.sendChat(roomName, chat, status);

                    String[] roomData = null;
                    String chatId = "";
                    Integer likeCount = 0;
                    while ((response = br.readLine()) != null) {
                        System.out.println(response);

                        HashMap<String, String> parseResponse = parseResponse(response);
                        String command = parseResponse.get("Command");
                        String body = parseResponse.get("Body");

                        if (command.equals(RECEIVE_CHAT_SUCCESS.name())) {
                            chatId = parseBody(body).get("ChatId");
                            likeCount = Integer.parseInt(parseBody(body).get("LikeCount"));
                            break;
                        }
                    }
                    chatInput.setText(""); // 입력 필드 초기화

                    String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
                    String formattedMessage = "[" + timestamp + "] " + userName + " : " + chat;

                    // 메시지를 상태에 따라 출력
                    addMessage(formattedMessage, status, chatId, likeCount);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "메시지 전송 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        frame.setVisible(true);
    }


    private void loadChatHistory() {
        String[] roomData = null;
        String response;

        HashMap<String, String> parseResponse = new HashMap<>();
        String command = " ";
        String body = " ";

        try {
            chatHandler.getChaHistory(roomName);

            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                System.out.println(response);

                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");

                if (command.equals(GET_CHAT_HISTORY_SUCCESS.name()))
                    break;

                // 쉼표로 채팅 기록 분리

                String[] chatEntries = response.split("\n");
                for (String chatEntry : chatEntries) {
                    String timestamp = parseBody(body).get("TimeStamp");
                    String userName = parseBody(body).get("UserName");
                    String content = parseBody(body).get("Content");
                    String chatStatus = parseBody(body).get("Status");
                    Integer chatLikeCount = Integer.parseInt(parseBody(body).get("LikeCount"));
                    String chatHistoryId = parseBody(body).get("ChatId");

                    System.out.println("chatHistoryId = " + chatHistoryId);
                    System.out.println("chatStatus = " + chatStatus);

                    // 포맷된 메시지 생성
                    String formattedMessage = "[" + timestamp + "] " + userName + " : " + content;
                    // 메시지를 상태에 따라 UI에 표시 (EDT에서 실행)
                    SwingUtilities.invokeLater(() -> {
                        addMessage(formattedMessage, chatStatus, chatHistoryId, chatLikeCount);
                    });

                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "채팅 기록 불러오기 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addMessage(String message, String status, String chatId, Integer like) {

        String response;
        HashMap<String, String> parseResponse = new HashMap<>();
        String command = " ";
        String body = " ";

        try {
            roomHandler.findRoom(roomName);
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");

                if (command.equals(SEND_ROOM_DATA_SUCCESS.name()))
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        String firstStatus = parseBody(body).get("FirstStatus");
        String secondStatus = parseBody(body).get("SecondStatus");

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new GridBagLayout()); // 그리드백 레이아웃 사용
        messagePanel.setBackground(Color.WHITE); // 배경색 설정

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // 수평으로 확장
        //gbc.insets = new Insets(1, 0, 0, 1); // 여백 설정

        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true); // 단어 기준 줄바꿈
        messageArea.setLineWrap(true); // 자동 줄바꿈
        messageArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        //messageArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messageArea.setBackground(Color.WHITE);

        // 메시지 높이 동적 계산
        int chatAreaWidth = status1ChatArea.getWidth() - 120; // 버튼 영역 고려
        messageArea.setSize(new Dimension(chatAreaWidth, Short.MAX_VALUE)); // 폭 설정
        messageArea.setPreferredSize(new Dimension(chatAreaWidth, messageArea.getPreferredSize().height));

        // 메시지 영역을 패널에 추가
        gbc.gridx = 0; // 첫 번째 열
        gbc.weightx = 0; // 가로로 확장
        gbc.anchor = GridBagConstraints.WEST; // 왼쪽 정렬
        gbc.insets = new Insets(0, 0, 0, 10);
        messagePanel.add(messageArea, gbc);

        // 메시지 기본 높이 계산
        FontMetrics metrics = messageArea.getFontMetrics(messageArea.getFont());
        int lineHeight = metrics.getHeight(); // 줄 높이 계산

        String emojiHeart = "❤";
        JButton likeButton = new JButton(emojiHeart + " "+ like);
        likeButton.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
        likeButton.setFocusPainted(false);
        likeButton.setBackground(Color.WHITE);
        likeButton.setForeground(Color.RED);  // 텍스트와 이모지 색을 빨간색으로 설정
        //likeButton.setPreferredSize(new Dimension(likeButton.getPreferredSize().width, lineHeight-5)); // 기존 너비 유지

        likeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    chatHandler.likeChat(roomName, chatId);

                    String likeResponse;
                    HashMap<String, String> parseResponse = new HashMap<>();
                    String command = " ";
                    String body = " ";
                    while ((likeResponse = br.readLine()) != null) {
                        System.out.println(likeResponse);

                        parseResponse = parseResponse(likeResponse);
                        command = parseResponse.get("Command");
                        body = parseResponse.get("Body");

                        if (command.equals(LIKE_CHAT_SUCCESS.name())) {
                            Integer likeCount = Integer.parseInt(parseBody(body).get("LikeCount"));
                            likeButton.setText(emojiHeart + " " + likeCount);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "좋아요 처리 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 좋아요 버튼을 패널에 추가
        gbc.gridx = 1; // 두 번째 열
        gbc.weightx = 0; // 버튼은 고정 크기
        gbc.anchor = GridBagConstraints.EAST; // 오른쪽 정렬
        gbc.insets = new Insets(0, 0, 0, 0);
        messagePanel.add(likeButton, gbc);

        // 메시지 패널 크기 동적 설정
        int panelHeight = Math.max(messageArea.getPreferredSize().height, likeButton.getPreferredSize().height);
        messagePanel.setMaximumSize(new Dimension(status1ChatArea.getWidth(), panelHeight));
        messagePanel.setPreferredSize(new Dimension(status1ChatArea.getWidth(), panelHeight));

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setPreferredSize(new Dimension(status1ChatArea.getWidth(), panelHeight));
        emptyPanel.setMaximumSize(new Dimension(status1ChatArea.getWidth(), panelHeight));

        if (status.equals(firstStatus)) {
            status1ChatArea.add(messagePanel); // 메시지 추가
            status2ChatArea.add(emptyPanel);
        } else if (status.equals(secondStatus)) {
            status2ChatArea.add(messagePanel); // 메시지 추가
            status1ChatArea.add(emptyPanel);

        }

        // UI 갱신
        status1ChatArea.revalidate();
        status1ChatArea.repaint();
        status2ChatArea.revalidate();
        status2ChatArea.repaint();

        // 양쪽 스크롤바 자동 이동
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane1 = (JScrollPane) status1ChatArea.getParent().getParent();
            JScrollPane scrollPane2 = (JScrollPane) status2ChatArea.getParent().getParent();

            JScrollBar verticalScrollBar1 = scrollPane1.getVerticalScrollBar();
            JScrollBar verticalScrollBar2 = scrollPane2.getVerticalScrollBar();

            verticalScrollBar1.setValue(verticalScrollBar1.getMaximum());
            verticalScrollBar2.setValue(verticalScrollBar2.getMaximum());
        });
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
        exitDialog.add(exitTitle, BorderLayout.NORTH);

        String[] roomData = null;
        String response;
        HashMap<String, String> parseResponse = new HashMap<>();
        String command = " ";
        String body = " ";

        try {
            roomHandler.findRoom(roomName);
            // 서버 응답 처리
            while ((response = br.readLine()) != null) {
                System.out.println(response);
                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");

                if (command.equals(FIND_ROOM_SUCCESS.name()))
                    break;
                roomData = response.split(",");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        String firstStatus = parseBody(body).get("FirstStatus");
        String secondStatus = parseBody(body).get("SecondStatus");

        // STATUS 버튼 패널
        JPanel statusPanel = new JPanel();
        statusPanel.setBounds(70, 80, 200, 40);
        statusPanel.setLayout(new GridLayout(1, 2, 10, 0));

        JButton status1Button = new JButton(firstStatus);
        JButton status2Button = new JButton(secondStatus);

        status1Button.setToolTipText(firstStatus);
        status2Button.setToolTipText(secondStatus);

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
            status1Button.setBackground(new Color(173, 216, 230));
            status2Button.setBackground(Color.WHITE);
        });

        status2Button.addActionListener(event -> {
            selectedStatus[0] = secondStatus;
            status1Button.setBackground(Color.WHITE);
            status2Button.setBackground(new Color(173, 216, 230));
        });

        status1Button.setFocusPainted(false);
        status2Button.setFocusPainted(false);

        statusPanel.add(status1Button);
        statusPanel.add(status2Button);
        exitDialog.add(statusPanel, BorderLayout.CENTER);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(115, 150, 100, 40);
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
                MainScreen mainScreen = new MainScreen(userName, sock, pw, br);
                mainScreen.createMainScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(exitDialog, "퇴장 처리 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitDialog.add(confirmButton, BorderLayout.SOUTH);
        exitDialog.setLocationRelativeTo(parentFrame);
        exitDialog.setVisible(true);
    }

    private void startMessageListener() {

        Timer messagePollingTimer = new Timer(100, e -> {
            try {
                HashMap<String, String> parseResponse = new HashMap<>();
                String command = " ";
                String body = " ";

                while (br.ready()) { // 메시지가 준비된 경우만 처리
                    String response = br.readLine();
                    System.out.println(response);

                    parseResponse = parseResponse(response);
                    command = parseResponse.get("Command");
                    body = parseResponse.get("Body");
                    if (response != null && command.equals(RECEIVE_CHAT_SUCCESS.name())) {
                        String timestamp = parseBody(body).get("TimeStamp");
                        String userName = parseBody(body).get("UserName");
                        String message = parseBody(body).get("Message");
                        String status = parseBody(body).get("Stauts");
                        Integer likeCount = Integer.parseInt(parseBody(body).get("LikeCount"));
                        String chatId = parseBody(body).get("ChatId");

                        String formattedMessage = "[" + timestamp + "] " + userName + " : " + message;

                        // UI 업데이트
                        addMessageToUI(formattedMessage, status, chatId, likeCount);
                    }
                }
            } catch (Exception ex) {
                System.out.println("메시지 수신 중 오류 발생: " + ex.getMessage());
            }
        });

        messagePollingTimer.start(); // 타이머 시작
    }

    private void addMessageToUI(String message, String messageStatus, String chatId, int likeCount) {
        SwingUtilities.invokeLater(() -> addMessage(message, messageStatus, chatId, likeCount));
    }

    private HashMap<String, String> parseBody(String body) {
        HashMap<String, String> parsedData = new HashMap<>();

        // 정규 표현식 수정
        Pattern pattern = Pattern.compile("(\\w+)\\s*:\\s*(.*?)(?=\\s*\\w+\\s*:\\s*|$)");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            String key = matcher.group(1).trim();  // Key
            String value = matcher.group(2).trim(); // Value
            parsedData.put(key, value);
        }

        return parsedData;
    }

    private HashMap<String, String> parseResponse(String request) {
        HashMap<String, String> result = new HashMap<>();

        // 정규 표현식: Command 뒤의 모든 내용을 Body로 처리
        Pattern pattern = Pattern.compile("(?:\\[Response\\])?UserName\\s*:\\s*(.+?)\\s*Command\\s*:\\s*(.+?)\\s*Body\\s*:\\s*(.*)");
        Matcher matcher = pattern.matcher(request);

        if (matcher.find()) {
            // 매칭된 값을 HashMap에 저장
            result.put("UserName", matcher.group(1).trim());
            result.put("Command", matcher.group(2).trim());
            result.put("Body", matcher.group(3).trim());
        }

        return result;
    }
}