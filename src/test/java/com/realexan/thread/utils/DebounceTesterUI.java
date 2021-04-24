/**
 * 
 */
package com.realexan.thread.utils;

import static com.realexan.common.ThreadUtils.now;
import static com.realexan.common.ThreadUtils.stackTraceToString;

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import com.realexan.common.ThrowingRunnable;
import com.realexan.thread.Debouncer;
import com.realexan.thread.Debouncer.Debounce;

/**
 * Tests the Debounce functionality through a simple UI.
 * 
 * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
 * @version
 *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
 *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
 *          <td width="10%"><b>Date</b></td>
 *          <td width="10%"><b>Author</b></td>
 *          <td width="10%"><b>Version</b></td>
 *          <td width="*"><b>Description</b></td>
 *          </tr>
 *          <tr bgcolor="white" id="TableRowColor">
 *          <td>23-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class DebounceTesterUI extends JFrame {

    private final String name = "realexan-test";

    private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS ");

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JButton run = new JButton("Run");

    private JLabel coolOffLabel = new JLabel("Cool Off(ms)");

    private JTextField coolOff = new JTextField();

    private JLabel forcedRunLabel = new JLabel("Forced run interval(ms)");

    private JTextField forcedRun = new JTextField();

    private JLabel immediateLabel = new JLabel("Immmediate");

    private JRadioButton immediate = new JRadioButton();

    private JLabel nonBlockingLabel = new JLabel("Non-blocking");

    private JRadioButton nonBlocking = new JRadioButton();

    private JButton create = new JButton("Create");

    private static JTextArea area = new JTextArea();

    private long startTime = 0;

    private ThrowingRunnable function = () -> DebounceTesterUI
            .p(getDate() + "-" + Thread.currentThread() + ": " + (now() - startTime));

    private long _coolOff = 1000;

    private long _forcedRun = 5000;

    private boolean _immediate = true;

    private boolean _nonBlocking = false;

    private Debounce db = null;

    private boolean dirty = true;

    public DebounceTesterUI() {
        this.setTitle("Realexan Debouncer test");
        this.setBounds(100, 100, 900, 600);
        this.setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(run);
        this.add(coolOffLabel);
        this.add(coolOff);
        this.add(forcedRunLabel);
        this.add(forcedRun);
        this.add(immediateLabel);
        this.add(immediate);
        this.add(nonBlockingLabel);
        this.add(nonBlocking);
        this.add(create);

        run.setBounds(10, 10, 100, 80);

        coolOffLabel.setBounds(150, 20, 150, 30);
        coolOff.setBounds(300, 20, 100, 30);
        forcedRunLabel.setBounds(150, 50, 150, 30);
        forcedRun.setBounds(300, 50, 100, 30);
        immediateLabel.setBounds(450, 20, 100, 30);
        immediate.setBounds(540, 22, 100, 30);
        nonBlockingLabel.setBounds(450, 50, 100, 30);
        nonBlocking.setBounds(540, 52, 100, 30);

        create.setBounds(790, 10, 100, 80);

        area.setBounds(0, 0, 820, 500);

        run.setToolTipText("Runs the debounce function. Will automatically create a "
                + "debounce function if the configurations have changed.");
        coolOff.setToolTipText("The cool off time period between runs.");
        forcedRun.setToolTipText("The interval for forced execution of the function,"
                + " in case the triggers don't cease for too long. " + "A negative value means this is disregarded."
                + " A non negative value lesser than coolOffTime will cause to use coolOffTime instead.");
        immediate.setToolTipText("flag to denote whether to execute the function "
                + "immediately on the trigger or wait until cool off.");
        nonBlocking.setToolTipText("The executor to be used to run the function. If "
                + "null, the runs will happen on scheduler thread which " + "manages the cool off.");
        create.setToolTipText("Creates a debounce function with the configurations");

        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(Font.getFont(Font.SANS_SERIF));
        JScrollPane scroller = new JScrollPane(area);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        DefaultCaret caret = (DefaultCaret) area.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scroller.setBounds(10, 100, 880, 400);
        this.add(scroller);

        setDefaults();
        // createNew();

        this.setResizable(false);
        this.setVisible(true);

        run.addActionListener((e) -> {
            if (dirty) {
                createNew();
                dirty = false;
            }
            if (startTime == 0) {
                startTime = now();
            }
            db.run();
        });

        create.addActionListener((e) -> createNew());

        KeyListener dirtyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                dirty = true;
            }
        };
        coolOff.addKeyListener(dirtyListener);
        forcedRun.addKeyListener(dirtyListener);
        immediate.addActionListener((e) -> dirty = true);
        nonBlocking.addActionListener((e) -> dirty = true);

    }

    private void createNew() {
        try {
            long lCoolOff = Long.parseLong(coolOff.getText());
            long lforcedRun = Long.parseLong(forcedRun.getText());
            boolean limmediate = immediate.isSelected();
            boolean lnonblocking = nonBlocking.isSelected();
            Debounce newDb = Debouncer.create(name, function, lCoolOff, lforcedRun, limmediate, lnonblocking);
            _coolOff = lCoolOff;
            _forcedRun = lforcedRun;
            _immediate = limmediate;
            _nonBlocking = lnonblocking;
            if (db != null) {
                db.cancel();
            }
            db = newDb;

            // p("\n");
            p("------------------------------------------------------------------------------");
            p("New Debounce function created with values (" + _coolOff + ", " + _forcedRun + ", " + _immediate + ", "
                    + _nonBlocking + ")");
            p("------------------------------------------------------------------------------");
        } catch (Exception e) {
            p(stackTraceToString(e));
        }
        setDefaults();

    }

    private void setDefaults() {
        coolOff.setText("" + _coolOff);
        forcedRun.setText("" + _forcedRun);
        immediate.setSelected(_immediate);
        nonBlocking.setSelected(_nonBlocking);
    }

    public static void main(String... args) throws Exception {
        new DebounceTesterUI();

    }

    public static String getDate() {
        return formatter.format(new Date());
    }

    public static void p(String s) {
        String txt = area.getText();
        if (!txt.isEmpty()) {
            txt = txt + "\n" + s;
        } else {
            txt = s;
        }
        area.setText(txt);
        System.out.println(s);
    }

}
