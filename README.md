latest -> [4D Viewer Web](https://github.com/dearsip/FourDViewerWeb)

# 4D-Action-Game

![4D-Action-Game](https://gyazo.com/2d3cec1396f93f7429b9649d9aef2899.png)

[4D Blocks Version 6](http://www.urticator.net/blocks/v6/index.html)（[権利表記](http://www.urticator.net/essay/3/373.html)）の改変・機能追加。

## Links
- [原作者による解説](http://www.urticator.net/maze/)
- [追加要素の紹介動画](https://youtu.be/i9g9Q2sXKv8)

## 起動方法
- [Java SE Development Kit](https://www.oracle.com/technetwork/java/javase/downloads/index.html)をインストールする。
- `4DAction.jar`と`current.properties`を同ディレクトリに置いて`4DAction.jar`を実行。（`current.properties`は各種設定を保存している。）
- `levels`及び`data`内のファイルはゲーム内の`Menu > Load`から開く。`data`内のファイルを開くには、`data`が`4DAction.jar`と同じディレクトリに置かれている必要がある。（`data/lib`内のファイルをインポートするため。）

## コンパイル
```shell
$ git clone https://github.com/dearsip/4D-Action-Game.git
$ cd src
$ javac *.java
$ jar -cfm 4DAction.jar MANIFEST.MF *.class *.properties
```

## 操作方法
### 基本操作
- `W,E,R,S,D,F,A,Z`:移動
- `U,I,O,J,K,L`:カメラ操作
- スペースキー: ブロック選択、ジャンプ
- `N`:ブロック配置、発砲

### Optionで変更できないキーの対応表

機能|オリジナル|本ソフト
--:|--:|--:
境界脱色|A|ctrl+Q
リロード|ctrl+R|ctrl+R
次のファイル|page down|J
前のファイル|page up|K
反転|ctrl+N|ctrl+N
透過|ctrl+S|ctrl+T
クリック|space|space
選択マーク非表示|ctrl+H|ctrl+H
シャッフル|ctrl+W|ctrl+S
ブロック追加|(shift+)insert|(shift+)N
ブロック削除|delete|M
ペイント|P|ctrl+P
トレイン|X-C-V|X-C-V
レール変更|Q|B
魚眼|ctrl+F|ctrl+F

キーコンフィグはゲーム内の`Meny > Options > Keys`（こちらは変更内容が`current.properties`に保存される）とソースコードの`KeyMapper.java`から可能。操作の詳細はそれぞれ[Keys](http://www.urticator.net/maze/ref-keys.html)、[Controls](http://www.urticator.net/blocks/v6/controls.html)を参照。

## LICENSE
[MIT](https://github.com/dearsip/4D-Action-Game/blob/master/LICENSE)
