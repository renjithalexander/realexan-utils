/**
 * 
 */
package com.realexan.thread.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

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
 *          <td>24-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class DebounceTester extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JButton button = new JButton("Run");

    private static JTextArea area = new JTextArea();

    Debounce db = Debouncer.prepare(() -> DebounceTester.p("" + System.currentTimeMillis() / 1000), 3000, 5000, true);

    public DebounceTester() {
        this.setBounds(100, 100, 900, 600);
        this.setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(button);
        this.add(area);
        button.setBounds(10, 10, 100, 80);
        area.setBounds(10, 100, 850, 400);
        this.setVisible(true);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println(e);
                db.run();
            }
        });
    }

    public static void main(String... args) throws Exception {
        DebounceTester t = new DebounceTester();

    }

    public static void p(String s) {
        String txt = area.getText() + "\n" + s;
        area.setText(txt);
        System.out.println(s);
    }

}
