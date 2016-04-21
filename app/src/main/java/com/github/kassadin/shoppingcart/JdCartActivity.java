package com.github.kassadin.shoppingcart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kassadin.shoppingcart.entity.JdResp;
import com.github.kassadin.shoppingcart.entity.JdResp.CartInfoBean;
import com.github.kassadin.shoppingcart.entity.JdResp.CartInfoBean.VendorsBean;
import com.github.kassadin.shoppingcart.entity.JdResp.CartInfoBean.VendorsBean.SortedBean;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class JdCartActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.expandable_list) ExpandableListView mExpandableList;
    @Bind(R.id.cb_all) CheckBox mCbAll;
    @Bind(R.id.total_price) TextView mTotalPrice;
    @Bind(R.id.total_price_final) TextView mTotalPriceFinal;
    @Bind(R.id.doit) Button mDoit;

    CartInfoBean cartInfo = new CartInfoBean();
    JdAdapter mJdAdapter = new JdAdapter();

    public static void start(Context context) {
        Intent starter = new Intent(context, JdCartActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jd_cart);
        ButterKnife.bind(this);

        setUpToolbar();
        setUpExpandableListView();
        refresh();
    }

    private void refresh() {
        Gson gson = new Gson();
        try {
            InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("jd.txt"));
            JdResp jdResp = gson.fromJson(isr, JdResp.class);
            if (jdResp != null) {
                cartInfo = jdResp.getCartInfo();
                mJdAdapter.notifyDataSetChanged();
                for (int i = 0; i < mJdAdapter.getGroupCount(); i++) {
                    mExpandableList.expandGroup(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpExpandableListView() {
        mExpandableList.setAdapter(mJdAdapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
    }

    // 全选
    @OnCheckedChanged(R.id.cb_all)
    public void onSelectedAll(boolean checked) {
        int groupCount = mJdAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            VendorsBean vb = (VendorsBean) mJdAdapter.getGroup(i);
            vb.checked = checked;
            int childrenCount = mJdAdapter.getChildrenCount(i);
            for (int j = 0; j < childrenCount; j++) {
                SortedBean child = (SortedBean) mJdAdapter.getChild(i, j);
                child.checked = checked;
            }
        }
        mJdAdapter.notifyDataSetChanged();
    }

    class JdAdapter extends BaseExpandableListAdapter {
        public static final String IMAGE_DOMAIN = "http://m.360buyimg.com/mobilecms/s300x300_";

        ImageLoader mImageLoader = ImageLoader.getInstance();

        @Override
        public int getGroupCount() {
            return cartInfo == null ? 0 : cartInfo.getSpecialId();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int childrenSize = cartInfo == null ? 0 : cartInfo.getVendors().get(groupPosition).getSorted().size();
            Log.d("jd", "childrenSize:" + childrenSize);
            return childrenSize;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return cartInfo.getVendors().get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return ((VendorsBean) getGroup(groupPosition)).getSorted().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            final ItemHeaderHolder headerHolder;
            final VendorsBean vendorsBean = (VendorsBean) getGroup(groupPosition);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.jd_item_shop_header, parent, false);
                headerHolder = new ItemHeaderHolder(convertView);
                convertView.setTag(headerHolder);
                headerHolder.mShopCbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (!buttonView.isPressed()) {
                            return;
                        }

                        VendorsBean vb = (VendorsBean) headerHolder.mShopCbSelectAll.getTag();
                        vb.checked = buttonView.isChecked();
                        // child 状态
                        List<SortedBean> sorted = vb.getSorted();
                        for (SortedBean sortedBean : sorted) {
                            sortedBean.checked = vb.checked;
                        }
                        notifyDataSetChanged();
                    }
                });
            } else {
                headerHolder = (ItemHeaderHolder) convertView.getTag();
            }

            headerHolder.mShopCbSelectAll.setTag(vendorsBean);

            final String shopName = vendorsBean.getShopName();
            headerHolder.mShopTitle.setText(shopName);
            headerHolder.mShopTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(JdCartActivity.this, shopName, Toast.LENGTH_SHORT).show();
                }
            });

            headerHolder.mShopCbSelectAll.setChecked(vendorsBean.checked);

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ItemNormalHolder normalHolder;
            Log.d("jd", "getChildView: " + childPosition);
            final SortedBean child = (SortedBean) getChild(groupPosition, childPosition);
            final VendorsBean vendorsBean = (VendorsBean) getGroup(groupPosition);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.jd_item_normal, parent, false);
                normalHolder = new ItemNormalHolder(convertView);
                convertView.setTag(normalHolder);
                normalHolder.mItemCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SortedBean sb = (SortedBean) normalHolder.mItemCbSelect.getTag();
                        sb.checked = isChecked;

                        VendorsBean vendorsBean = (VendorsBean) normalHolder.mItemCbSelect.getTag(R.id.tag_group);
                        // 全组选中
                        vendorsBean.checked = isChecked && isGroupSeletedAll(vendorsBean);
                        notifyDataSetChanged();
                    }
                });
            } else {
                normalHolder = (ItemNormalHolder) convertView.getTag();
            }

            normalHolder.mItemCbSelect.setTag(child);
            normalHolder.mItemCbSelect.setTag(R.id.tag_group, vendorsBean);
            String imgUrl;
            if (child.getItemType() == 1) {
                normalHolder.mHuangouLayout.setVisibility(View.GONE);
                normalHolder.mItemName.setText(child.getItem().Name);
                normalHolder.mItemPrice.setText(child.getItem().getPriceShow());
                imgUrl = child.getItem().ImgUrl;
            } else { // 12 换购类型
                SortedBean.ItemBean.SkusBean skusBean = child.getItem().getSkus().get(0);
                // 换购
                normalHolder.mHuangouLayout.setVisibility(View.VISIBLE);
                // 名字
                normalHolder.mItemName.setText(skusBean.getName());
                // 价格
                normalHolder.mItemPrice.setText(skusBean.getPriceShow());
                imgUrl = skusBean.getImgUrl();
            }

            normalHolder.mItemCbSelect.setChecked(child.checked);
            //    "ImgUrl": "jfs/t2194/167/1979359591/285546/5d01b53/56e7841bN59290866.jpg!q70.jpg.webp",
            imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf("!"));
            imgUrl = IMAGE_DOMAIN + imgUrl;
            Log.d("main", imgUrl);

            String imgTag = (String) normalHolder.mItemImg.getTag();
            if (imgUrl.equals(imgTag)) {

            } else {
                mImageLoader.displayImage(imgUrl, normalHolder.mItemImg);
                normalHolder.mItemImg.setTag(imgUrl);
            }

            return convertView;
        }

        private boolean isGroupSeletedAll(VendorsBean vb) {
            boolean flag = true;
            List<SortedBean> sorted = vb.getSorted();
            for (SortedBean sortedBean : sorted) {
                if (!sortedBean.checked) {
                    flag = false;
                    break;
                }
            }

            return flag;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return cartInfo.isIsEmpty();
        }

    }

    // 商店
    class ItemHeaderHolder {
        @Bind(R.id.shop_cb_select_all) CheckBox mShopCbSelectAll;
        @Bind(R.id.shop_title) TextView mShopTitle;

        public ItemHeaderHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    // 商品
    class ItemNormalHolder {

        @Bind(R.id.huangou_layout) RelativeLayout mHuangouLayout;
        @Bind(R.id.item_cb_select) CheckBox mItemCbSelect;
        @Bind(R.id.item_img) ImageView mItemImg;
        @Bind(R.id.item_name) TextView mItemName;
        @Bind(R.id.item_price) TextView mItemPrice;

        public ItemNormalHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
