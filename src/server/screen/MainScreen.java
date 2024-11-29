package screen;

import server.controller.ChatController;
import server.domain.Room;
import server.repository.RoomRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.util.*;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.List;

public class MainScreen {
    private final RoomRepository roomRepository;
    private final ChatController chatController;
    private final String nickname; // 사용자 닉네임
    private JFrame frame;
    private DefaultTableModel tableModel; // 테이블 모델
    private Socket sock;
    private PrintWriter pw;
    private BufferedReader br;

    public MainScreen(String nickname, Socket sock, PrintWriter pw, BufferedReader br) {
        this.sock = sock;
        this.pw = pw;
        this.br = br;
        this.nickname = nickname;
        this.roomRepository = new RoomRepository();

        // 정확한 타입으로 초기화
        Map<String, List<PrintWriter>> userMap = new HashMap<>();
        this.chatController = new ChatController(userMap); // ChatController 초기화
        //this.chatController = new ChatController(); // ChatController 초기화

    }

    public void createMainScreen() {
        if (this.frame == null) {
            this.frame = new JFrame("토론 플랫폼 - 메인 화면");
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 255, 255)); // 흰색

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 255)); // 흰색
        JLabel titleLabel = new JLabel("토론 채팅방 리스트");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 업데이트 버튼
        JButton updateButton = new JButton("업데이트");
        updateButton.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        updateButton.setPreferredSize(new Dimension(200, 40)); // 버튼 크기 설정
        updateButton.addActionListener(e -> {
            // 방 리스트 갱신 로직
            try {
                String response;
                // 서버에 방 리스트 요청
                pw.println("/list");
                pw.flush();
                // 서버 응답 처리
                tableModel.setRowCount(0); // 기존 데이터 초기화
                while ((response = br.readLine()) != null) {
                    System.out.println("클라이언트: 받은 데이터 = " + response);
                    if (response.equals("END")) break;
                    String[] roomData = response.split(",");
                    tableModel.addRow(new Object[]{roomData[0], roomData[1], roomData[2], roomData[3]});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        topPanel.add(updateButton);

        JButton createRoomButton = new JButton("+");
        createRoomButton.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        createRoomButton.setBackground(Color.WHITE);
        createRoomButton.setFocusPainted(false);
        createRoomButton.addActionListener(e -> createRoom(frame)); // 채팅방 생성 팝업
        topPanel.add(createRoomButton, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // 테이블 생성
        String[] columnNames = {"토론방 이름", "생성자", "찬성", "반대"};
        tableModel = new DefaultTableModel(columnNames, 0); // 테이블 모델 초기화
        JTable table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(220, 220, 220)); // 연한 회색
        table.getTableHeader().setForeground(Color.BLACK);

        // 테이블 셀 중앙 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        try {
            String response;
            // 서버에 방 리스트 요청
            pw.println("/list");
            pw.flush();
            // 서버 응답 처리
            tableModel.setRowCount(0); // 기존 데이터 초기화
            while ((response = br.readLine()) != null) {
                if (response.equals("END")) break;
                String[] roomData = response.split(",");
                tableModel.addRow(new Object[]{roomData[0], roomData[1], roomData[2], roomData[3]});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "방 목록 갱신 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(tableScrollPane, BorderLayout.CENTER);

        // 하단 입장 버튼
        JButton enterButton = new JButton("입장");
        enterButton.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        enterButton.addActionListener(e -> {
            System.out.println("입장 버튼 호출 확인");
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String roomName = (String) table.getValueAt(selectedRow, 0);
                System.out.println("입장 버튼 roomName 확인: " + roomName);
                showEnterRoomPopup(roomName, frame); // 팝업 표시 후 입장
            } else {
                JOptionPane.showMessageDialog(frame, "입장할 방을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(enterButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);


        frame.setVisible(true);
    }

    private void createRoom(JFrame parentFrame) {
        // 방 생성 팝업
        JDialog dialog = new JDialog(parentFrame, "토론방 생성", true);
        dialog.setSize(350, 300);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(new Color(245, 245, 245)); // 연한 회색 배경

        // 제목 라벨
        JLabel titleLabel = new JLabel("토론방 생성");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 10, 350, 30);
        dialog.add(titleLabel);

        // 입력 필드
        JTextField topicField = new JTextField();
        topicField.setBounds(50, 60, 250, 30);
        topicField.setToolTipText("주제를 입력하세요.");
        dialog.add(topicField);

        JTextField status1Field = new JTextField("찬성");
        status1Field.setBounds(50, 110, 120, 30);
        status1Field.setToolTipText("상태1 입력 (기본값: 찬성)");
        dialog.add(status1Field);

        JTextField status2Field = new JTextField("반대");
        status2Field.setBounds(180, 110, 120, 30);
        status2Field.setToolTipText("상태2 입력 (기본값: 반대)");
        dialog.add(status2Field);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(125, 180, 100, 30);
        confirmButton.addActionListener(e -> {
            String topic = topicField.getText().trim();
            String status1 = status1Field.getText().trim();
            String status2 = status2Field.getText().trim();

            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "주제를 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                System.out.println("Sending command to server: /c " + topic + " " + status1 + " " + status2);
                pw.println("/c " + topic + " " + status1 + " " + status2);
                pw.flush();
                Room newRoom = new Room(topic, status1, status2, nickname);
                //roomRepository.createRoom(newRoom);
                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{
                            newRoom.getRoomName(),
                            nickname,
                            newRoom.getFirstStatusCount(),
                            newRoom.getSecondStatusCount()
                    });
                });

                JOptionPane.showMessageDialog(dialog, "채팅방이 생성되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "채팅방 생성 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(confirmButton);
        dialog.setVisible(true);
    }

    private void showEnterRoomPopup(String roomName, JFrame parentFrame) {
        if (parentFrame == null) {
            System.out.println("Error: MainScreen.frame is null.");
            return;
        }
        // 방 입장 팝업
        JDialog dialog = new JDialog(parentFrame, "토론방 입장", true);
        dialog.setSize(350, 250);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(new Color(230, 230, 230)); // 연한 회색 배경

        // 제목 라벨
        JLabel titleLabel = new JLabel("토론방 입장");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 10, 350, 30);
        titleLabel.setForeground(Color.BLACK); // 검정색 텍스트
        dialog.add(titleLabel);

        // 상태 버튼 추가
        Room room = roomRepository.findRoomByName(roomName);
        System.out.println("showEnterRoomPopup에서 roomName 확인: " + roomName);
        System.out.println("showEnterRoomPopup에서 room 확인: " + room);

        if (room == null) {
            JOptionPane.showMessageDialog(dialog, "방 정보를 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            return;
        }

        // 상태 버튼 생성
        JButton status1Button = new JButton(room.getFirstStatus());
        JButton neutralButton = new JButton("중립");
        JButton status2Button = new JButton(room.getSecondStatus());

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

        dialog.add(status1Button);
        dialog.add(neutralButton);
        dialog.add(status2Button);

        // 선택된 상태 추적 변수
        final String[] selectedStatus = {null};

        // 버튼 클릭 이벤트 (색상 변경)
        status1Button.addActionListener(e -> {
            selectedStatus[0] = room.getFirstStatus();
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
            selectedStatus[0] = room.getSecondStatus();
            status1Button.setBackground(Color.WHITE); // 다른 버튼은 흰색
            neutralButton.setBackground(Color.WHITE); // 다른 버튼은 흰색
            status2Button.setBackground(new Color(173, 216, 230)); // 연한 파란색
        });

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        confirmButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        confirmButton.setBounds(125, 150, 100, 40);
        confirmButton.setBackground(new Color(200, 200, 200)); // 연한 회색 버튼
        confirmButton.setForeground(Color.BLACK); // 검정 텍스트

        confirmButton.addActionListener(e -> {
            if (selectedStatus[0] != null) {
                try {
                    System.out.println("입장 확인 명령 보내는거 확인, roomName: " + roomName);
                    System.out.println("입장 확인 명령 보내는거 확인, selectedStatus[0]: " + selectedStatus[0]);
                    String response;
                    pw.println("/e " + roomName + " " + selectedStatus[0]);
                    pw.flush();
                    // 채팅 내역 요청
                    Map<String, List<PrintWriter>> userMap = new HashMap<>(); // 빈 맵 초기화
                    while ((response = br.readLine()) != null) {
                        if (response.equals("END")) break;
                        System.out.println("클라이언트 받은 데이터: " + response);
                    }
                    String enteredRoomName = response;
                    screen.ChatRoomScreen chatRoomScreen = new screen.ChatRoomScreen(roomName, nickname, sock, pw, br, userMap, selectedStatus[0]);
                    chatRoomScreen.createChatRoomScreen();
                    dialog.dispose();
                    if (parentFrame != null) {
                        parentFrame.dispose();
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "채팅방 입장 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "상태를 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(confirmButton);

        dialog.setVisible(true);
    }

}