package androdevians.remotecontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    int key = 0;
    float dX = 0;
    float dY = 0;
    int type = 0;
    private Socket socket;
    private DataOutputStream out;
    private GestureDetector mousePadGestureDetector;
    private GestureDetector scrollWheelGestureDetector;
    private EditText InputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mousePadGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                type = 1;
                dX = 0;
                dY = 0;
                new thread().start();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                type = 1;
                dX = -distanceX;
                dY = -distanceY;
                new thread().start();
                return true;
            }
        });

        scrollWheelGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                type = 2;
                dY = -distanceY;
                dY/=5;
//                Log.e("onScroll: ", distanceX + " " + distanceY);
                new thread().start();
                return true;
            }
        });


        View mousePad = findViewById(R.id.root);
        mousePad.setOnTouchListener(this);
        mousePad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        View scrollWheel = findViewById(R.id.scroll_wheel);
        scrollWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return scrollWheelGestureDetector.onTouchEvent(event);
            }
        });
        scrollWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        InputEditText = (EditText) findViewById(R.id.text_input);
        InputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                key = event.getKeyCode();
                send();
                return false;
            }
        });
        InputEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    key = 8;
                    send();
                }
                return false;
            }
        });
        InputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start < s.length() && s.length() > 0) {
                    key = Character.toUpperCase(s.charAt(start));
                    send();

                    InputEditText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void send() {
        type = 0;
        new thread().start();
    }

    public void sendUp(View view) {
        key = 38;
        send();
    }

    public void sendDown(View view) {
        key = 40;
        send();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mousePadGestureDetector.onTouchEvent(event);
    }


    private class thread extends Thread {
        @Override
        public void run() {
            try {
                if (socket == null) {
                    socket = new Socket("192.168.100.100", 5000);
                    socket.setKeepAlive(true);
                }
                if (socket.isClosed()) {
                    socket = new Socket("192.168.100.100", 5000);
                    socket.setKeepAlive(true);
                }

                if (out == null)
                    out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(type);
                if (type == 0) {
                    out.writeInt(key);
                } else if (type == 1) {
                    out.writeFloat(dX);
                    out.writeFloat(dY);
                } else if (type == 2) {
                    out.writeFloat(dY);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
