# 4D-Action-Game

![4D-Action-Game](https://gyazo.com/2d3cec1396f93f7429b9649d9aef2899.png)

[4D Blocks Version 6](http://www.urticator.net/blocks/v6/index.html)（[権利表記](http://www.urticator.net/essay/3/373.html)）の改変・機能追加。

## Links
- [原作者による解説](http://www.urticator.net/maze/)
- [解説動画（自作）](https://www.nicovideo.jp/watch/sm31889569)
- [プレイ動画（自作）](https://www.nicovideo.jp/watch/sm33133173)

## 起動方法
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
- `W,E,R,S,D,F,A,Z`:移動
- `U,I,O,J,K,L`:カメラ操作
- スペースキー: ブロック選択、ジャンプ
- `N`:ブロック配置、発砲

キーコンフィグはゲーム内の`Meny > Options > Keys`（こちらは変更内容が`current.properties`に保存される）とソースコードの`KeyMapper.java`から可能。操作の詳細はそれぞれ[Keys](http://www.urticator.net/maze/ref-keys.html)、[Controls](http://www.urticator.net/blocks/v6/controls.html)を参照。

## LICENSE
[MIT](https://github.com/dearsip/4D-Action-Game/blob/master/LICENSE)
