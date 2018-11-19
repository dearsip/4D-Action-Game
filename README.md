# 4D-Action-Game

![4D-Action-Game](https://gyazo.com/2d3cec1396f93f7429b9649d9aef2899.png)

[4D Blocks Version 6](http://www.urticator.net/blocks/v6/index.html)（[権利表記](http://www.urticator.net/essay/3/373.html)）の改変・機能追加。

## Links
- [原作者による解説](http://www.urticator.net/maze/)
- [解説動画（自作）](https://www.nicovideo.jp/watch/sm31889569)
- [プレイ動画（自作）](https://www.nicovideo.jp/watch/sm33133173)

## 起動方法
`4DAction.jar`と`current.properties`を同ディレクトリに置いて`4DAction.jar`を実行。

`levels`にあるのはそのバージョンで遊ぶことを想定されたファイル。ゲーム内の`Menu > Load`から開く。

## 操作方法
### 主な操作（一部のモードのみ）
- `W,E,R,S,D,F,A,Z`:移動
- `U,I,O,J,K,L`:カメラ操作
- スペースキー:ジャンプ
- `ctrl+R`:リスタート
- `ctrl+J`:次のファイル
- `ctrl+K`:前のファイル
### その他
キーコンフィグはゲーム内の`Meny > Options > Keys`（こちらは変更内容が`current.properties`に保存される）とソースコードの`KeyMapper.java`から可能。
このデータでは以下の図の通り変更を加えてある。

![control](https://gyazo.com/eca1b5e73f611e05db2bcd918d363c9f.png)
![control2](https://gyazo.com/ed846ed6ae92a6355b13e4d253f8fa6f.png)

操作の詳細はそれぞれ[Keys](http://www.urticator.net/maze/ref-keys.html)、[Controls](http://www.urticator.net/blocks/v6/controls.html)を参照。

## 開発目標
ゲームとして成立している程度の完成度の4Dゲームの開発。

## 更新予定
- 十字テクスチャの実装（実験）
- 描画距離制限の実装（実験）
- マウス+ホイールによるカメラ操作の実装
