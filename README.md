## 购物车 Demo

面试的时候被问到购物车的实现，之前没做过，一直以为 ListView 嵌套 ListView。

回来看了下 jd 的布局，ExpandableListView，原来你们都这么玩的…

抓了下 jd 的数据，格式是嵌套的

```
shop : {
	shopinfo:{
		店铺信息
	}，
	
	items:[
		下属的几个商品
		item1,
		item2,
	]
}
```
用 ExpandableListView 实现。


xcf 的数据格式是平级的：

```
[{
	shop:{
	},
	item:{
	}
}]
```

用 ListView 实现。
