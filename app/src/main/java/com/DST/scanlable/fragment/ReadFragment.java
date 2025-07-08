package com.DST.scanlable.fragment;

import static com.DST.scanlable.ConnectServer.CONNECTSERVER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DST.scanlable.R;
import com.DST.scanlable.Reader;
import com.DST.scanlable.adapter.MyAdapter;
import com.DST.scanlable.model.Product;
import com.DST.scanlable.model.TagInfo;
import com.DST.scanlable.utils.ExcelUtil;
import com.rfid.InventoryTagMap;
import com.rfid.trans.ReadTag;
import com.rfid.trans.ReaderParameter;
import com.rfid.trans.TagCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;


public class ReadFragment extends Fragment implements OnClickListener {
    private static final String TAG = "LJH";
    Button scan;
    RecyclerView listView;
    TextView txNum, txTime, txtCount, txtSpeed;
    long beginTime = 0;
    private Timer timer;
    private MyAdapter myAdapter;
    private static final int SCAN_INTERVAL = 1000;
    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MSG_UPDATE_TIME = 1;
    private static final int MSG_UPDATE_ERROR = 2;
    private static final int MSG_UPDATE_STOP = 3;
    private static boolean isReader = false;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean isStopThread = false;
    public static int ErrorCount;
    public static int ErrorCRC;
    public Map<String, Integer> dtIndexMap = new LinkedHashMap<>();
    private List<Product> products = new ArrayList<>();
    private List<InventoryTagMap> tags = new ArrayList<>();
    private Context mContext;
    private Runnable calcSpeedRunnable = null;
    private Handler calcSeepHandler = new Handler();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private int time = 0;
    private Map<String, Long> rateMap = new Hashtable<>();
    int asciiNum;
    ReaderParameter readerParameter;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private Map<String, Product> productMap = new HashMap<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case MSG_UPDATE_LISTVIEW:
                        Product product = (Product) msg.obj;
                        if (product != null) {
                            tags = Reader.rrlib.getInventoryTagMapList();
                            updateProductList(product);
                            myAdapter.notifyData(products, dtIndexMap);
                            txNum.setText((products.size()) + " (" + tags.size() + ")");
                        }
                        break;
                    case MSG_UPDATE_TIME:
                        txTime.post(() -> {
                            String toTime = secToTime(++time);
                            txTime.setText(toTime + " (s)");
                        });
                        long before = 0;
                        long after;
                        Long afterValue = rateMap.get("after");
                        if (null != afterValue) {
                            before = afterValue;
                        }
                        long readCount = getReadCount(myAdapter.getData());
                        rateMap.put("after", readCount);
                        after = readCount;
                        if (after >= before) {
                            long rateValue = after - before;
                            txtSpeed.post(() -> txtSpeed.setText(rateValue + " (t/s)"));
                        }
                        break;
                    case MSG_UPDATE_ERROR:
                        break;
                    case MSG_UPDATE_STOP:
                        scan.setText(getString(R.string.btscan));
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.toString();
            }
        }
    };

    private void updateProductList(Product product) {
        // If the product is already in the list, we don't need to add it again
        if (productMap.containsKey(product.getPCode())) {
            return;
        }

        // Add the product to our maps
        productMap.put(product.getPCode(), product);
        products.add(product);
    }

    @SuppressLint("HandlerLeak")
    final Handler handlerStop = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                calcSeepHandler.removeCallbacks(calcSpeedRunnable);
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        scan = view.findViewById(R.id.button_scan);
        scan.setOnClickListener(this);
        listView = view.findViewById(R.id.product_real_list_view);
        products = new ArrayList<>();
        txNum = view.findViewById(R.id.tx_num);
        txTime = view.findViewById(R.id.tx_time);
        txtCount = view.findViewById(R.id.txt_errorcount);
        txtSpeed = view.findViewById(R.id.txt_speed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.addItemDecoration(new DividerItemDecoration(mContext, 1));

        myAdapter = new MyAdapter(mContext, new ArrayList<>(products), dtIndexMap);
        listView.setAdapter(myAdapter);

        time = 0;
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        long startTime = SystemClock.elapsedRealtime();
        isStopThread = false;
        readerParameter = Reader.rrlib.GetInventoryParameter();
        long endTime = SystemClock.elapsedRealtime();
        Log.e("耗时3", "耗时3=" + (endTime - startTime));
    }


    @Override
    public void onClick(View arg0) {
        if (arg0.getId() == R.id.button_scan) {
            readRfid();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void readRfid() {
        if (!isReader) {
            try {
                Log.e("TAG", "onClick: ");
                if (timer == null) {
                    if (isStopThread) return;
                    isStopThread = true;
                    Reader.rrlib.getInventoryTagMapList().clear();
                    Reader.rrlib.getInventoryTagResultList().clear();
                    dtIndexMap = new LinkedHashMap<>();
                    MsgCallback callback = new MsgCallback();
                    Reader.rrlib.SetCallBack(callback);
                    ErrorCount = 0;
                    ErrorCRC = 0;

                    if (Reader.rrlib.StartRead() == 0) {

                        isReader = true;
                        rateMap = new Hashtable<>();
                        if (myAdapter != null) {
                            txNum.setText("0");
                            txTime.setText("0");
                            txtCount.setText("");

                            time = 0;
                            myAdapter.notifyDataSetChanged();
                            mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                            mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                        }
                        beginTime = System.currentTimeMillis();
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.removeMessages(MSG_UPDATE_TIME);
                                mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                            }
                        }, 0, SCAN_INTERVAL);
                        scan.setText(getString(R.string.btstop));
                    }
                } else {
                    cancelScan();
                }
            } catch (Exception e) {
                cancelScan();
            }
        } else {
            cancelScan();
        }
    }

    public void cancelScan() {
        Reader.rrlib.StopRead();
        isReader = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scan.setText(getString(R.string.btscan));
        }
    }

    public void exportExcel() {
        if (!isReader) {
            List<PermissionItem> permissionItems = new ArrayList<>();
            permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.txt_storage), R.drawable.permission_ic_storage));
            HiPermission.create(mContext)
                    .title(getString(R.string.txt_inport_excel))
                    .permissions(permissionItems)
                    .checkMutiPermission(new PermissionCallback() {
                        @Override
                        public void onClose() {
                        }

                        @Override
                        public void onFinish() {
                            String filePath = "/sdcard/excel_Tags/";
                            String fileName = "Product_" + dateFormat.format(new Date()) + ".xls";
                            String[] title = {"Product Code", "Product Name", "Brand", "Category", "Price", "Tag Count"};
                            if (!products.isEmpty()) {
                                ExcelUtil.initExcel(filePath, fileName, title, mContext);

                                ExcelUtil.writeObjListToExcel(getProductData(products), filePath + fileName, this);
                                Toast.makeText(mContext, "Export path: " + (filePath + fileName), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "No Data", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onDeny(String permission, int position) {
                        }

                        @Override
                        public void onGuarantee(String permission, int position) {
                        }
                    });
        } else {
            Toast.makeText(mContext, getString(R.string.read_card_being), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchProductInfo(String epc) {
        executorService.submit(() -> {
            try {
                SharedPreferences preferences = mContext.getSharedPreferences(CONNECTSERVER, Context.MODE_PRIVATE);
                String server = preferences.getString("SERVER", null);

                if (server == null) {
                    Toast.makeText(mContext, "Set server connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                URL url = new URL(server +"/getProducts/" + epc);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String jsonResponse = response.toString();
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    // Check if there's an error in the response
                    if (!jsonObject.has("error")) {
                        String pcode = jsonObject.getString("pcode");
                        String hscode = jsonObject.getString("hscode");
                        String pdesc = jsonObject.getString("pdesc");
                        String brand = jsonObject.getString("brand");
                        String category = jsonObject.getString("category");
                        double price = jsonObject.getDouble("price");

                        Product product;
                        if (productMap.containsKey(pcode)) {
                            product = productMap.get(pcode);
                            if (product != null) {
                                product.addTag(new TagInfo(epc));
                            }
                        } else {
                            product = new Product(pcode, hscode, pdesc, brand, category, price);
                            product.addTag(new TagInfo(epc));
                        }

                        Message msg = mHandler.obtainMessage(MSG_UPDATE_LISTVIEW, product);
                        mHandler.sendMessage(msg);
                    } else {
                        Log.e(TAG, "Error getting product: " + jsonObject.getString("message"));
                    }
                } else {
                    Log.e(TAG, "HTTP Error: " + responseCode);
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching product: " + e.getMessage());
            }
        });
    }

    public ArrayList<ArrayList<String>> getProductData(List<Product> products) {
        ArrayList<ArrayList<String>> recordList = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ArrayList<String> beanList = new ArrayList<>();
            Product product = products.get(i);
            beanList.add(product.getPCode());
            beanList.add(product.getPDesc());
            beanList.add(product.getBrand());
            beanList.add(product.getCategory());
            beanList.add(String.valueOf(product.getPrice()));
            beanList.add(String.valueOf(product.getTagCount()));
            recordList.add(beanList);
        }
        return recordList;
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        Log.e("TAG", "onKeyDown: " + keyCode);
        if (keyCode == 305 || keyCode == 619 || keyCode == 621) {
            readRfid();
        }
    }

    public class MsgCallback implements TagCallback {
        @Override
        public void tagCallback(ReadTag arg0) {
            if (arg0 != null) {
                String epc = arg0.epcId.toUpperCase();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                int encodingFormat = preferences.getInt("selectedFormat",readerParameter.AsciiPtr);
                if (encodingFormat == 1 && readerParameter.TidLen == 0) {
                    asciiNum = 1;
                } else {
                    asciiNum = 0;
                }
                if (asciiNum == 1){
                    epc = hexToAscii(epc);
                }
                InventoryTagMap m;

                Integer findIndex = dtIndexMap.get(epc);
                Log.d(TAG, "tagCallback: findIndex -->" + findIndex);
                if (findIndex == null) {
                    Reader.rrlib.beginSound(false);
                    dtIndexMap.put(epc, dtIndexMap.size());
                    m = new InventoryTagMap();
                    m.strEPC = epc;
                    m.antenna = arg0.antId;
                    m.strRSSI = String.valueOf(arg0.rssi);
                    m.nReadCount = 1;
                    Reader.rrlib.getInventoryTagMapList().add(m);
                    fetchProductInfo(epc);
                } else {
                    Reader.rrlib.beginSound(true);
                    m = Reader.rrlib.getInventoryTagMapList().get(findIndex);
                    m.antenna |= arg0.antId;
                    m.nReadCount++;
                    m.strRSSI = String.valueOf(arg0.rssi);
                }
            } else {
               Reader.rrlib.beginSound(false);
            }

            mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
            mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
        }

        @Override
        public int CRCErrorCallBack(int reason) {
            if (reason == 1) {
                ErrorCRC += 1;
            }
            ErrorCount += 1;
            mHandler.removeMessages(MSG_UPDATE_ERROR);
            mHandler.sendEmptyMessage(MSG_UPDATE_ERROR);
            return 0;
        }

        @Override
        public void FinishCallBack() {
            // TODO Auto-generated method stub
            isStopThread = false;
            mHandler.removeMessages(MSG_UPDATE_STOP);
            mHandler.sendEmptyMessage(MSG_UPDATE_STOP);
        }

        @Override
        public int tagCallbackFailed(int reason) {
            return 0;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            cancelScan();
        }
    }

    @Override
    public void onPause() {
        cancelScan();
        super.onPause();
    }

    public String secToTime(long time) {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        time = time * 1000;
        return formatter.format(time);
    }

    private long getReadCount(List<Product> tagInfoList) {
        long readCount = 0;
        for (int i = 0; i < tagInfoList.size(); i++) {
            readCount += tagInfoList.get(i).getTagCount();
        }
        return readCount;
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }
}
