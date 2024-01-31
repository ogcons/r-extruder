import React from "react";
import ReactDOM from "react-dom/client";
import "./index.scss";
import Rextruder from "./containers/Rextruder";
import "@basf/basf-header-footer";

import { TBasfFooter, TBasfHeader } from "./utils/types";

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace JSX {
    interface IntrinsicElements {
      "basf-header": React.DetailedHTMLProps<
        React.HTMLAttributes<HTMLElement> & TBasfHeader,
        HTMLElement
      >;
      "basf-footer": React.DetailedHTMLProps<
        React.HTMLAttributes<HTMLElement> & TBasfFooter,
        HTMLElement
      >;
    }
  }
}

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <React.StrictMode>
    <Rextruder />
  </React.StrictMode>
);
