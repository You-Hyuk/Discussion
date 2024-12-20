package client.screen;

import client.handler.RoomHandler;
import server.domain.Room;
import server.repository.RoomRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.*;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static server.dto.ErrorResponseCommand.FIND_ROOM_FAILED;
import static server.dto.SuccessResponseCommand.*;

public class MainScreen {
    private final RoomRepository roomRepository;
    private final String userName; // 사용자 닉네임
    private JFrame frame;
    private DefaultTableModel tableModel; // 테이블 모델
    private Socket sock;
    private PrintWriter pw;
    private BufferedReader br;
    private RoomHandler roomHandler;

    public MainScreen(String userName, Socket sock, PrintWriter pw, BufferedReader br) {
        this.sock = sock;
        this.pw = pw;
        this.br = br;
        this.userName = userName;
        this.roomRepository = new RoomRepository();
        this.roomHandler = new RoomHandler(pw, userName);
    }


    // 방 리스트 갱신 로직
    private void refreshRoomTable() {
        try {
            HashMap<String, String> parseResponse = new HashMap<>();
            String command = " ";
            String body = " ";

            roomHandler.getRoomList();

            // 서버 응답 처리
            tableModel.setRowCount(0); // 기존 데이터 초기화
            String response;
            while ((response = br.readLine()) != null) {
                System.out.println(response);
                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");

                if (command.equals(GET_ROOM_LIST_SUCCESS.name()))
                    break; // 응답 종료
                if (command.equals(SEND_ROOM_DATA_SUCCESS.name())) {
                    tableModel.addRow(new Object[]{
                            parseBody(body).get("RoomName"),
                            parseBody(body).get("UserName"),
                            parseBody(body).get("FirstStatus") + " : " + parseBody(body).get("FirstStatusCount") + " vs " + parseBody(body).get("SecondStatus") + " : " + parseBody(body).get("SecondStatusCount")
                    });
                }
            }
        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void createMainScreen() {
        UIManager.put("ToolTip.font", new Font("Malgun Gothic", Font.PLAIN, 20)); // 원하는 크기와 폰트 설정
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Color.GRAY)); // 테두리 설정

        if (this.frame == null) {
            this.frame = new JFrame("토론 플랫폼 - 메인 화면");
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        //topPanel.setBackground(new Color(190, 190, 190)); // 회색, "토론 채팅방 리스트"
        JLabel titleLabel = new JLabel("토론 채팅방 리스트");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // 오른쪽 정렬, 버튼 간 간격 10
        //buttonPanel.setBackground(new Color(190, 190, 190)); // 버튼 영역 배경색, 회색

        // 업데이트 버튼
        JButton updateButton = new JButton("↻");
        updateButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 20));
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(e -> {
            // 방 리스트 갱신 로직
            refreshRoomTable();
        });

        // 생성 버튼
        JButton createRoomButton = new JButton("+");
        createRoomButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 20));
        //createRoomButton.setBackground(new Color(140, 140, 140)); 생성 버튼 색, 회색
        createRoomButton.setFocusPainted(false);
        createRoomButton.addActionListener(e -> createRoomPopup(frame)); // 채팅방 생성 팝업

        buttonPanel.add(updateButton, BorderLayout.EAST);
        buttonPanel.add(createRoomButton, BorderLayout.EAST);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // 테이블 생성
        String[] columnNames = {"토론방 이름", "생성자", "투표 현황"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(210, 210, 210));
        table.getTableHeader().setForeground(Color.black); //테이블 헤더 글씨색
        table.getTableHeader().setReorderingAllowed(false);

        // 테이블 셀 중앙 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        refreshRoomTable();

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(tableScrollPane, BorderLayout.CENTER);

        // 하단 입장 버튼
        JButton enterButton = new JButton("입장");
        enterButton.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        enterButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String roomName = (String) table.getValueAt(selectedRow, 0);
                enterRoomPopup(roomName, frame); // 팝업 표시 후 입장
            } else {
                JOptionPane.showMessageDialog(frame, "입장할 방을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(enterButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

//        refreshRoomTable();
        frame.setVisible(true);
    }

    private void createRoomPopup(JFrame parentFrame) {
        // 방 생성 팝업
        JDialog dialog = new JDialog(parentFrame, "토론방 생성", true);
        dialog.setSize(350, 300);
        dialog.setLayout(null);
        //dialog.getContentPane().setBackground(Color.white); // 토론방 생성 배경 색

        // 제목 라벨
        JLabel titleLabel = new JLabel("토론방 생성");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 10, 350, 30);
        dialog.add(titleLabel);

        // 입력 필드
        JTextField topicField = new JTextField();
        topicField.setBounds(50, 60, 250, 30);
        topicField.setForeground(Color.GRAY); // 플레이스홀더 글씨 색상
        topicField.setText("주제를 입력하세요."); // 플레이스홀더 텍스트
        // 플레이스홀더 이벤트
            topicField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!topicField.isFocusable()) {
                    topicField.setFocusable(true); // 클릭 시 포커스 가능하도록 변경
                    topicField.requestFocusInWindow(); // 포커스 요청
                }
                if (topicField.getText().equals("주제를 입력하세요.")) {
                    topicField.setText(""); // 플레이스홀더 제거
                    topicField.setForeground(Color.BLACK); // 텍스트 색상 변경
                }
            }
        });

        // FocusListener 추가
        topicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                if (topicField.getText().isEmpty()) {
                    topicField.setText("주제를 입력하세요."); // 플레이스홀더 복원
                    topicField.setForeground(Color.GRAY); // 텍스트 색상 회색으로 변경
                }
            }
        });
        dialog.add(topicField);

        JTextField status1Field = new JTextField("찬성");
        status1Field.setBounds(50, 110, 120, 30);
        dialog.add(status1Field);

        JTextField status2Field = new JTextField("반대");
        status2Field.setBounds(180, 110, 120, 30);
        dialog.add(status2Field);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(125, 180, 100, 30);
        confirmButton.addActionListener(e -> {
            String topic = topicField.getText().trim();
            String status1 = status1Field.getText().trim();
            String status2 = status2Field.getText().trim();

            if (topic.isEmpty() || topic.equals("주제를 입력하세요.")) {
                JOptionPane.showMessageDialog(dialog, "주제를 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                roomHandler.createRoom(topic, status1, status2);

                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{
                            topic, userName, status1 + " : " + 0 + " vs " + status2 + " : " + 0
                    });
                });

                JOptionPane.showMessageDialog(dialog, "채팅방이 생성되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "채팅방 생성 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(confirmButton);

        // 다이얼로그의 기본 포커스 설정 (주제 입력 필드로 기본 포커스가 가지 않도록)
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                dialog.getContentPane().requestFocusInWindow(); // 다이얼로그 자체에 포커스 설정
            }
        });

        dialog.setLocationRelativeTo(parentFrame); // 창 중앙에 표시

        dialog.setVisible(true);
    }

    private void enterRoomPopup(String roomName, JFrame parentFrame) {
        if (parentFrame == null) {
            System.out.println("Error: MainScreen.frame is null.");
            return;
        }
        String[] roomData = null;
        HashMap<String, String> parseResponse = new HashMap<>();
        String command = " ";
        String body = " ";
        List<Room> rooms = new ArrayList<>();
        try {
            roomHandler.findRoom(roomName);
            String response;
            while ((response = br.readLine()) != null) {
                System.out.println(response);
                parseResponse = parseResponse(response);
                command = parseResponse.get("Command");
                body = parseResponse.get("Body");
                if (command.equals(SEND_ROOM_DATA_SUCCESS.name()))
                    break;
                else if (command.equals(FIND_ROOM_FAILED.name())) {
                    JOptionPane.showMessageDialog(parentFrame, response, "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                roomData = response.split(",");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, "서버와의 통신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 방 입장 팝업
        JDialog dialog = new JDialog(parentFrame, "토론방 입장", true);
        dialog.setSize(350, 300);
        dialog.setLayout(null);
        //dialog.getContentPane().setBackground(Color.white); //입장 팝업 배경색

        // 제목 라벨
        JLabel titleLabel = new JLabel("토론방 입장");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 10, 350, 30);
        titleLabel.setForeground(Color.BLACK); // 검정색 텍스트
        dialog.add(titleLabel);

        String firstStatus = parseBody(body).get("FirstStatus");
        String secondStatus = parseBody(body).get("SecondStatus");

        // 상태 버튼 생성
        JButton status1Button = new JButton(firstStatus);
        JButton neutralButton = new JButton("중립");
        JButton status2Button = new JButton(secondStatus);

        // 버튼 초기 스타일
        status1Button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        neutralButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        status2Button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));

        status1Button.setBackground(Color.WHITE); // 기본 흰색
        neutralButton.setBackground(Color.WHITE); // 기본 흰색
        status2Button.setBackground(Color.WHITE); // 기본 흰색

        status1Button.setBounds(50, 80, 80, 40);
        neutralButton.setBounds(135, 80, 80, 40);
        status2Button.setBounds(220, 80, 80, 40);

        status1Button.setFocusPainted(false);
        neutralButton.setFocusPainted(false);
        status2Button.setFocusPainted(false);

        dialog.add(status1Button);
        dialog.add(neutralButton);
        dialog.add(status2Button);

        // 선택된 상태 추적 변수
        final String[] selectedStatus = {null};

        // 버튼 클릭 이벤트 (색상 변경)
        status1Button.addActionListener(e -> {
            selectedStatus[0] = firstStatus;
            status1Button.setBackground(new Color(173, 216, 230)); // 연한 파란색
            neutralButton.setBackground(Color.WHITE); // 다른 버튼은 흰색
            status2Button.setBackground(Color.WHITE); // 다른 버튼은 흰색
        });

        neutralButton.addActionListener(e -> {
            selectedStatus[0] = "중립";
            status1Button.setBackground(Color.WHITE); // 다른 버튼은 흰색
            neutralButton.setBackground(new Color(173, 216, 230)); // 연한 파란색
            status2Button.setBackground(Color.WHITE); // 다른 버튼은 흰색
        });

        status2Button.addActionListener(e -> {
            selectedStatus[0] = secondStatus;
            status1Button.setBackground(Color.WHITE); // 다른 버튼은 흰색
            neutralButton.setBackground(Color.WHITE); // 다른 버튼은 흰색
            status2Button.setBackground(new Color(173, 216, 230)); // 연한 파란색
        });

        status1Button.setToolTipText(firstStatus);
        status2Button.setToolTipText(secondStatus);


        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(125, 150, 100, 40);
        confirmButton.setBackground(Color.white); // 입장 확인 버튼 색
        confirmButton.setForeground(Color.BLACK); // 입장 확인 버튼 텍스트 색

        confirmButton.addActionListener(e -> {
            if (selectedStatus[0] != null) {
                try {
                    String response;
                    roomHandler.enterRoom(roomName, selectedStatus[0]);

                    // 채팅 내역 요청
                    while ((response = br.readLine()) != null) {
                        System.out.println(response);
                        HashMap<String, String> parsedResponse = parseResponse(response);
                        String enterCommand = parsedResponse.get("Command");

                        if (enterCommand.equals(ENTER_ROOM_SUCCESS.name())) {
                            break;
                        }
                    }
                    if (roomName != null) {
                        // ChatRoomScreen 생성
                        ChatRoomScreen chatRoomScreen = new ChatRoomScreen(roomName, userName, sock, pw, br, selectedStatus[0]);
                        chatRoomScreen.createChatRoomScreen();

                        dialog.dispose();
                        parentFrame.dispose();
                        refreshRoomTable();

                    } else {
                        JOptionPane.showMessageDialog(dialog, "방 이름을 확인할 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "채팅방 입장 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "상태를 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(confirmButton);

        dialog.setLocationRelativeTo(parentFrame); // 창 중앙에 표시

        dialog.setVisible(true);
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